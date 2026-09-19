package com.starter.development.adapter.in.web;

import java.util.List;

public record CsvImportResponse(
        int acceptedCount,
        int rejectedCount,
        List<CsvImportError> errors
) {
}

record CsvImportError(int rowNumber, String reason) {
}