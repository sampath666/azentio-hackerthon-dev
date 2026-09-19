package com.starter.development.application.service;

import com.starter.development.application.port.in.TransactionUseCase;
import com.starter.development.application.port.out.TransactionRepositoryPort;
import com.starter.development.application.port.out.AccountRepositoryPort;
import com.starter.development.application.port.out.AlertRepositoryPort;
import com.starter.development.domain.model.Alert;
import com.starter.development.domain.model.Account;
import com.starter.development.domain.model.Transaction;
import com.starter.development.domain.model.TransactionIngestionResult;
import com.starter.development.domain.model.BulkTransactionResult;
import com.starter.development.domain.model.RejectedTransaction;
import com.starter.development.domain.model.TransactionCommand;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class TransactionService implements TransactionUseCase {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepositoryPort repository;
    private final AccountRepositoryPort accountRepository;
    private final AlertRepositoryPort alertRepository;
    private final Map<Long, Object> accountLocks = new ConcurrentHashMap<>();

    @Value("${sentinel.rules.large-transaction-threshold-inr:10000}")
    private double largeTransactionThresholdInr;
    @Value("${sentinel.rules.structuring-minimum:3}")
    private int structuringMinimum;
    @Value("${sentinel.rules.structuring-lower-inr:9000}")
    private double structuringLowerInr;
    @Value("${sentinel.rules.structuring-upper-inr:10000}")
    private double structuringUpperInr;
    @Value("${sentinel.rules.structuring-window-hours:24}")
    private long structuringWindowHours;
    @Value("${sentinel.rules.rapid-movement-window-hours:48}")
    private long rapidMovementWindowHours;
    @Value("${sentinel.rules.rapid-movement-percentage:0.80}")
    private double rapidMovementPercentage;
    @Value("${sentinel.rules.behavior-window-days:90}")
    private long behaviorWindowDays;
    @Value("${sentinel.rules.behavior-multiplier:3}")
    private double behaviorMultiplier;
    @Value("${sentinel.rules.high-risk-jurisdictions:AF,IR,KP,MM,RU,SY}")
    private String highRiskJurisdictionsConfig;
    @Value("${sentinel.exchange-rates-inr:INR=1,USD=83,EUR=90,GBP=105}")
    private String exchangeRatesConfig;

    public TransactionService(TransactionRepositoryPort repository,
                              AccountRepositoryPort accountRepository,
                              AlertRepositoryPort alertRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    @Transactional
    public TransactionIngestionResult createTransaction(Long accountId, Double amount, String currency,
                                                        String timestamp, String type, String counterparty,
                                                        String jurisdiction, String channel) {
        Object accountLock = accountLocks.computeIfAbsent(accountId == null ? 0L : accountId, key -> new Object());
        synchronized (accountLock) {
            logger.info("Ingesting transaction for accountId={}, amount={}, currency={}, type={}",
                    accountId, amount, currency, type);
            return createTransactionInternal(accountId, amount, currency, timestamp, type,
                    counterparty, jurisdiction, channel);
        }
    }

    private TransactionIngestionResult createTransactionInternal(Long accountId, Double amount, String currency,
                                                                 String timestamp, String type, String counterparty,
                                                                 String jurisdiction, String channel) {
        if (accountId == null || amount == null || !Double.isFinite(amount) || amount <= 0
                || currency == null || currency.isBlank() || timestamp == null || timestamp.isBlank()
                || type == null || type.isBlank()) {
            throw new IllegalArgumentException("Account, positive amount, currency, timestamp, and type are required");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        String normalizedCurrency = currency.trim().toUpperCase(Locale.ROOT);
        if (!List.of("INR", "USD", "EUR", "GBP").contains(normalizedCurrency)) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
        String normalizedType = type.trim().toUpperCase(Locale.ROOT);
        if (!List.of("DEPOSIT", "WITHDRAWAL", "TRANSFER").contains(normalizedType)) {
            throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }
        try {
            Instant.parse(timestamp);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Timestamp must be ISO-8601, for example 2026-09-19T10:00:00Z");
        }

        double normalizedAmountInr = normalizeToInr(amount, normalizedCurrency);
        Transaction transaction = new Transaction(accountId.toString(), amount, normalizedCurrency, timestamp,
                normalizedType, counterparty, jurisdiction);
        Transaction savedTransaction = repository.save(transaction, accountId, normalizedAmountInr);
        savedTransaction = new Transaction(savedTransaction.getId(), savedTransaction.getAccount(),
            savedTransaction.getAmount(), savedTransaction.getCurrency(), normalizedAmountInr,
            savedTransaction.getTimestamp(), savedTransaction.getType(), savedTransaction.getCounterparty(),
            savedTransaction.getJurisdiction(), savedTransaction.getChannel(), savedTransaction.getCreatedDate());
        Alert alert = detectRules(savedTransaction, account);
        logger.info("Transaction persisted id={} for accountId={}, alertId={}", savedTransaction.getId(),
            accountId, alert == null ? null : alert.getId());
        return new TransactionIngestionResult(savedTransaction, alert);
    }

    @Override
    public BulkTransactionResult createBulkTransactions(List<TransactionCommand> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException("At least one transaction is required");
        }

        List<TransactionIngestionResult> acceptedTransactions = new ArrayList<>();
        List<RejectedTransaction> rejectedTransactions = new ArrayList<>();
        int alertedCount = 0;

        for (int index = 0; index < transactions.size(); index++) {
            TransactionCommand command = transactions.get(index);
            int rowNumber = index + 1;
            try {
                if (command == null) {
                    throw new IllegalArgumentException("Transaction record is required");
                }
                TransactionIngestionResult result = createTransaction(command.accountId(), command.amount(),
                        command.currency(), command.timestamp(), command.type(), command.counterparty(),
                        command.jurisdiction(), command.channel());
                acceptedTransactions.add(result);
                if (result.alert() != null) {
                    alertedCount++;
                }
            } catch (RuntimeException exception) {
                rejectedTransactions.add(new RejectedTransaction(rowNumber, safeMessage(exception)));
            }
        }

        return new BulkTransactionResult(acceptedTransactions.size(), rejectedTransactions.size(),
                alertedCount, acceptedTransactions, rejectedTransactions);
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    private double normalizeToInr(double amount, String currency) {
        Map<String, Double> rates = new HashMap<>();
        for (String entry : exchangeRatesConfig.split(",")) {
            String[] pair = entry.split("=", 2);
            if (pair.length == 2) rates.put(pair[0].trim().toUpperCase(Locale.ROOT), Double.valueOf(pair[1].trim()));
        }
        Double rate = rates.get(currency);
        if (rate == null) throw new IllegalArgumentException("Unsupported currency: " + currency);
        return amount * rate;
    }

        private Alert detectRules(Transaction transaction, Account account) {
        Instant transactionTime = Instant.parse(transaction.getTimestamp());
        List<RuleHit> hits = new ArrayList<>();
        String evidenceId = String.valueOf(transaction.getId());

        if (transaction.getNormalizedAmountInr() >= largeTransactionThresholdInr) {
            hits.add(new RuleHit("LARGE_TRANSACTION", 40,
                "Transaction exceeds the INR 10,000 reporting threshold.", List.of(evidenceId)));
        }

        List<Transaction> last24Hours = withCurrentTransaction(
                transactionsInWindow(account.getId(), transactionTime.minusSeconds(structuringWindowHours * 60 * 60), transactionTime), transaction);
        List<Transaction> structuringTransactions = last24Hours.stream()
            .filter(item -> amountInr(item) >= structuringLowerInr && amountInr(item) < structuringUpperInr)
            .toList();
        if (structuringTransactions.size() >= structuringMinimum) {
            hits.add(new RuleHit("STRUCTURING", 80,
                "At least three transactions between INR 9,000 and INR 9,999 occurred within 24 hours.",
                transactionIds(structuringTransactions)));
        }

        List<Transaction> last48Hours = withCurrentTransaction(
            transactionsInWindow(account.getId(), transactionTime.minusSeconds(rapidMovementWindowHours * 60 * 60), transactionTime), transaction);
        for (Transaction deposit : last48Hours) {
            if (!"DEPOSIT".equals(deposit.getType())) {
            continue;
            }
            double outgoing = last48Hours.stream()
                .filter(item -> ("WITHDRAWAL".equals(item.getType()) || "TRANSFER".equals(item.getType()))
                    && amountInr(item) > 0
                    && !Instant.parse(item.getTimestamp()).isBefore(Instant.parse(deposit.getTimestamp())))
                .mapToDouble(this::amountInr)
                .sum();
            if (outgoing >= amountInr(deposit) * rapidMovementPercentage) {
            hits.add(new RuleHit("RAPID_MOVEMENT", 75,
                "At least 80% of a deposit moved out within 48 hours.",
                transactionIds(List.of(deposit))));
            break;
            }
        }

        Set<String> highRiskJurisdictions = Set.of(highRiskJurisdictionsConfig.split(","));
        String jurisdiction = transaction.getJurisdiction() == null
            ? "" : transaction.getJurisdiction().trim().toUpperCase(Locale.ROOT);
        String counterparty = transaction.getCounterparty() == null
            ? "" : transaction.getCounterparty().toLowerCase(Locale.ROOT);
        if (highRiskJurisdictions.contains(jurisdiction)
            || counterparty.contains("sanction") || counterparty.contains("high-risk")) {
            hits.add(new RuleHit("HIGH_RISK_JURISDICTION", 90,
                "Transaction involves a configured high-risk jurisdiction or counterparty.", List.of(evidenceId)));
        }

        List<Transaction> historicalTransactions = customerTransactions(account, transactionTime.minusSeconds(behaviorWindowDays * 24 * 60 * 60), transactionTime)
            .stream().filter(item -> !String.valueOf(item.getId()).equals(evidenceId)).toList();
        if (!historicalTransactions.isEmpty()) {
            double historicalDailyAverage = historicalTransactions.stream()
                .filter(item -> amountInr(item) > 0)
                .mapToDouble(this::amountInr)
                .sum() / behaviorWindowDays;
            double dailyValue = customerTransactions(account, transactionTime.truncatedTo(java.time.temporal.ChronoUnit.DAYS),
                transactionTime.plusSeconds(24 * 60 * 60)).stream()
                .filter(item -> amountInr(item) > 0)
                .mapToDouble(this::amountInr)
                .sum();
            if (historicalDailyAverage > 0 && dailyValue > historicalDailyAverage * behaviorMultiplier) {
            hits.add(new RuleHit("BEHAVIORAL_DEVIATION", 60,
                "Daily customer transaction value exceeds three times the 90-day rolling average.",
                List.of(evidenceId)));
            }
        }

        List<Transaction> roundTransactions = last24Hours.stream()
            .filter(item -> amountInr(item) % 1000 == 0
                || (amountInr(item) >= 9000 && amountInr(item) < 10000))
            .toList();
        if (roundTransactions.size() >= 2) {
            hits.add(new RuleHit("ROUND_NUMBER_PATTERN", 25,
                "Repeated round-number or just-below-threshold transactions were detected.",
                transactionIds(roundTransactions)));
        }

        if (hits.isEmpty()) {
            return null;
        }

        String ruleType = hits.stream().map(RuleHit::name).distinct().collect(Collectors.joining(","));
        String explanation = hits.stream().map(RuleHit::explanation).distinct().collect(Collectors.joining(" "));
        String newEvidence = hits.stream().flatMap(hit -> hit.evidence().stream()).distinct()
            .collect(Collectors.joining(","));
        double score = Math.min(100, hits.stream().mapToInt(RuleHit::weight).sum());
        Long customerId = parseCustomerId(account.getCustomer());
        List<Alert> existingAlerts = alertRepository.findOpenByAccountId(account.getId());
        Alert existing = existingAlerts.isEmpty() ? null : existingAlerts.get(0);
        String evidence = existing == null || existing.getEvidenceTransactionIds() == null
            || existing.getEvidenceTransactionIds().isBlank()
            ? newEvidence
            : existing.getEvidenceTransactionIds() + "," + newEvidence;
        evidence = java.util.Arrays.stream(evidence.split(","))
            .filter(item -> !item.isBlank()).distinct().collect(Collectors.joining(","));
        Alert alert = new Alert(existing == null ? null : existing.getId(), customerId, account.getId(), ruleType,
            score, explanation, evidence, "OPEN",
            existing == null ? Instant.now().toString() : existing.getCreatedDate(), Instant.now().toString());
        return alertRepository.save(alert);
    }

        private List<Transaction> transactionsInWindow(Long accountId, Instant from, Instant to) {
        return repository.findByAccountId(accountId).stream()
            .filter(item -> {
                try {
                    Instant timestamp = Instant.parse(item.getTimestamp());
                    return !timestamp.isBefore(from) && !timestamp.isAfter(to);
                } catch (DateTimeParseException exception) {
                    return false;
                }
            })
            .toList();
        }

        private List<Transaction> withCurrentTransaction(List<Transaction> transactions, Transaction current) {
        if (transactions.stream().anyMatch(item -> String.valueOf(item.getId()).equals(String.valueOf(current.getId())))) {
            return transactions;
        }
        List<Transaction> result = new ArrayList<>(transactions);
        result.add(current);
        return result;
        }

        private double amountInr(Transaction transaction) {
        if (transaction.getNormalizedAmountInr() != null) {
            return transaction.getNormalizedAmountInr();
        }
        return normalizeToInr(transaction.getAmount(), transaction.getCurrency().toUpperCase(Locale.ROOT));
        }

        private List<Transaction> customerTransactions(Account account, Instant from, Instant to) {
        return accountRepository.findByCustomerId(parseCustomerId(account.getCustomer()))
            .stream().flatMap(item -> transactionsInWindow(item.getId(), from, to).stream()).toList();
        }

        private List<String> transactionIds(List<Transaction> transactions) {
        return transactions.stream().map(item -> String.valueOf(item.getId())).toList();
        }

        private Long parseCustomerId(String customer) {
        try {
            return Long.valueOf(customer);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Account has an invalid customer reference: " + customer);
        }
        }

        private record RuleHit(String name, int weight, String explanation, List<String> evidence) {
        }

    @Override
    public List<Transaction> getTransactions() { return repository.findAll(); }

    @Override
    public List<Transaction> getTransactionsByAccount(Long accountId, String from, String to) {
        String start = from == null ? "" : from;
        String end = to == null ? Instant.now().toString() : to;
        return repository.findByAccountIdAndTimestampBetween(accountId, start, end);
    }

    @Override
    public Transaction getTransaction(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }
}
