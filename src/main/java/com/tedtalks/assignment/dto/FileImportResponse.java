package com.tedtalks.assignment.dto;

public record FileImportResponse(long numberOfRecords, long successfulRecordCount, long failedRecordCount) { }
