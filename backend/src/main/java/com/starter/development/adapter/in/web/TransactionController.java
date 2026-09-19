package com.starter.development.adapter.in.web;

import com.starter.development.application.port.in.TransactionUseCase;
import com.starter.development.domain.model.Transaction;
import com.starter.development.domain.model.TransactionIngestionResult;
import com.starter.development.domain.model.BulkTransactionResult;
import com.starter.development.domain.model.TransactionCommand;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionUseCase transactionUseCase;

    public TransactionController(TransactionUseCase transactionUseCase) {
        this.transactionUseCase = transactionUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionIngestionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Transaction request body is required");
        }
        TransactionIngestionResult result = transactionUseCase.createTransaction(request.accountId(), request.amount(),
                request.currency(), request.timestamp(), request.type(), request.counterparty(),
                request.jurisdiction(), request.channel());
        return TransactionIngestionResponse.from(result);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public BulkTransactionResponse createBulkTransactions(
            @Valid @RequestBody List<@Valid CreateTransactionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("At least one transaction is required");
        }
        List<TransactionCommand> commands = requests.stream()
                .map(request -> request == null ? null : request.toCommand())
                .toList();
        BulkTransactionResult result = transactionUseCase.createBulkTransactions(commands);
        return BulkTransactionResponse.from(result);
    }

    @PostMapping(value = "/bulk/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BulkTransactionResponse createBulkTransactionsFromCsv(
            @RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is required");
        }

        try {
            List<TransactionCommand> commands = parseCsv(file);
            BulkTransactionResult result = transactionUseCase.createBulkTransactions(commands);
            return BulkTransactionResponse.from(result);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read CSV file", exception);
        }
    }

    @GetMapping
    public List<TransactionResponse> getTransactions(@RequestParam(required = false) Long accountId,
                                                     @RequestParam(required = false) String from,
                                                     @RequestParam(required = false) String to) {
        List<Transaction> transactions = accountId == null
                ? transactionUseCase.getTransactions()
                : transactionUseCase.getTransactionsByAccount(accountId, from, to);
        return transactions.stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable Long id) {
        return TransactionResponse.from(transactionUseCase.getTransaction(id));
    }

    private static List<TransactionCommand> parseCsv(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] lines = content.split("\\R");
        List<TransactionCommand> commands = new ArrayList<>();
        int start = lines.length > 0 && lines[0].trim().toLowerCase().startsWith("accountid") ? 1 : 0;

        for (int index = start; index < lines.length; index++) {
            String line = lines[index].trim();
            if (!line.isBlank()) {
                commands.add(parseCsvLine(line));
            }
        }
        return commands;
    }

    private static TransactionCommand parseCsvLine(String line) {
        String[] values = line.split(",", -1);
        return new TransactionCommand(
                parseLong(value(values, 0)),
                parseDouble(value(values, 1)),
                value(values, 2),
                value(values, 3),
                value(values, 4),
                value(values, 5),
                value(values, 6),
                value(values, 7)
        );
    }

    private static String value(String[] values, int index) {
        return index < values.length && !values[index].isBlank() ? values[index].trim() : null;
    }

    private static Long parseLong(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Double parseDouble(String value) {
        try {
            return value == null ? null : Double.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}

record CreateTransactionRequest(@NotNull Long accountId, @NotNull @Positive Double amount,
                                @NotBlank String currency, @NotBlank String timestamp,
                                @NotBlank String type, String counterparty, String jurisdiction, String channel) {

    TransactionCommand toCommand() {
        return new TransactionCommand(accountId, amount, currency, timestamp, type,
                counterparty, jurisdiction, channel);
    }
}

record TransactionIngestionResponse(TransactionResponse transaction, AlertResponse alert) {

    static TransactionIngestionResponse from(TransactionIngestionResult result) {
        return new TransactionIngestionResponse(TransactionResponse.from(result.transaction()),
                result.alert() == null ? null : AlertResponse.from(result.alert()));
    }
}
