package com.starter.development.adapter.in.web;

import com.starter.development.application.port.in.AccountUseCase;
import com.starter.development.application.port.in.CustomerUseCase;
import com.starter.development.domain.model.Account;
import com.starter.development.domain.model.Customer;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountUseCase accountUseCase;
    private final CustomerUseCase customerUseCase;

    public AccountController(AccountUseCase accountUseCase, CustomerUseCase customerUseCase) {
        this.accountUseCase = accountUseCase;
        this.customerUseCase = customerUseCase;
    }

    @PostMapping
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Account request body is required");
        }
        Account account = accountUseCase.createAccount(request.customerId(), request.accountNumber(),
                request.type(), request.currency(), request.openingDate(), request.status());
        return AccountResponse.from(account);
    }

    @GetMapping
    public List<AccountResponse> getAccounts(@RequestParam(required = false) Long customerId) {
        List<Account> accounts = customerId == null
                ? accountUseCase.getAccounts()
                : accountUseCase.getAccountsByCustomer(customerId);
        return accounts.stream().map(AccountResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        return AccountResponse.from(accountUseCase.getAccount(id));
    }

    @PostMapping(value = "/bulk/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public CsvImportResponse importAccounts(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Account CSV file is required");
        }

        try {
            List<CsvImportError> errors = new ArrayList<>();
            int accepted = 0;
            List<String[]> rows = parseCsv(file);
            for (int index = 1; index < rows.size(); index++) {
                try {
                    Map<String, String> row = rowMap(rows.get(0), rows.get(index));
                    Customer customer = customerUseCase.getCustomerByKycId(required(row, "customer_id"));
                    accountUseCase.createAccount(customer.getId(), required(row, "account_id"),
                            required(row, "account_type"), required(row, "currency"),
                            required(row, "open_date"), required(row, "account_status"));
                    accepted++;
                } catch (RuntimeException exception) {
                    errors.add(new CsvImportError(index + 1, message(exception)));
                }
            }
            return new CsvImportResponse(accepted, errors.size(), errors);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read account CSV file", exception);
        }
    }

    private static List<String[]> parseCsv(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] lines = content.split("\\R");
        if (lines.length < 2) {
            throw new IllegalArgumentException("Account CSV must contain a header and at least one row");
        }
        List<String[]> rows = new ArrayList<>();
        rows.add(lines[0].replace("\uFEFF", "").split(",", -1));
        for (int index = 1; index < lines.length; index++) {
            if (!lines[index].isBlank()) {
                rows.add(lines[index].split(",", -1));
            }
        }
        return rows;
    }

    private static Map<String, String> rowMap(String[] headers, String[] values) {
        Map<String, String> row = new HashMap<>();
        for (int index = 0; index < headers.length; index++) {
            row.put(headers[index].trim(), index < values.length ? values[index].trim() : "");
        }
        return row;
    }

    private static String required(Map<String, String> row, String column) {
        String value = row.get(column);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required column is missing: " + column);
        }
        return value;
    }

    private static String message(RuntimeException exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}

record CreateAccountRequest(Long customerId, String accountNumber, String type,
                            String currency, String openingDate, String status) {
}
