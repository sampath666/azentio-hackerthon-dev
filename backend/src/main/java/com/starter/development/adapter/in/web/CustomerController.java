package com.starter.development.adapter.in.web;

import com.starter.development.application.port.in.CustomerUseCase;
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
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerUseCase customerUseCase;

    public CustomerController(CustomerUseCase customerUseCase) {
        this.customerUseCase = customerUseCase;
    }

    @PostMapping
    public CustomerResponse createCustomer(@RequestBody CreateCustomerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Customer request body is required");
        }
        Customer customer = customerUseCase.createCustomer(request.name(), request.kycId(),
                request.riskRating(), request.country());
        return CustomerResponse.from(customer);
    }

    @GetMapping
    public List<CustomerResponse> getCustomers() {
        return customerUseCase.getCustomers().stream().map(CustomerResponse::masked).toList();
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        return CustomerResponse.from(customerUseCase.getCustomer(id));
    }

    @PostMapping(value = "/bulk/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public CsvImportResponse importCustomers(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Customer CSV file is required");
        }

        try {
            List<CsvImportError> errors = new ArrayList<>();
            int accepted = 0;
            List<String[]> rows = parseCsv(file);
            for (int index = 1; index < rows.size(); index++) {
                try {
                    Map<String, String> row = rowMap(rows.get(0), rows.get(index));
                    String customerId = required(row, "customer_id");
                    String name = required(row, "first_name") + " " + required(row, "last_name");
                    customerUseCase.createCustomer(name, customerId, required(row, "risk_rating"),
                            required(row, "country"));
                    accepted++;
                } catch (RuntimeException exception) {
                    errors.add(new CsvImportError(index + 1, message(exception)));
                }
            }
            return new CsvImportResponse(accepted, errors.size(), errors);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read customer CSV file", exception);
        }
    }

    private static List<String[]> parseCsv(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] lines = content.split("\\R");
        if (lines.length < 2) {
            throw new IllegalArgumentException("Customer CSV must contain a header and at least one row");
        }
        List<String[]> rows = new ArrayList<>();
        String[] headers = lines[0].replace("\uFEFF", "").split(",", -1);
        rows.add(headers);
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

record CreateCustomerRequest(String name, String kycId, String riskRating, String country) {
}
