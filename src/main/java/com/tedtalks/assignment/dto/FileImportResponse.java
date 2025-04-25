package com.tedtalks.assignment.dto;

public record FileImportResponse(long totalLines, long successfulImports, long failedRecordCount) { }
