package com.tedtalks.assignment.util;

import com.tedtalks.assignment.exception.InvalidFileTypeException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.apache.coyote.BadRequestException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@UtilityClass
public class FileUtil {

    private static final int MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    public static boolean validateCsvFile(MultipartFile file) throws FileSizeLimitExceededException {

        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("File is too large to be validated");
            throw new FileSizeLimitExceededException("File size exceeds maximum limit of 20MB", file.getSize(), MAX_FILE_SIZE);
        }

        if (!"text/csv".equals(file.getContentType())) {
            log.error("File is not a CSV file");
            throw new InvalidFileTypeException("Invalid media type");
        }

        return true;
    }

    public boolean verifyCsvRecord(final CSVRecord record, final DateTimeFormatter dateFormatter) {
        if (Objects.isNull(record)) {
            log.warn("The record is null. It will be skipped.");
            return false;
        }

        LocalDate eventDate = FileUtil.checkValidDate(record.get(2), dateFormatter);
        Long views = FileUtil.parseLongSafe(record.get(3));
        Long likes = FileUtil.parseLongSafe(record.get(4));

        if (Objects.isNull(eventDate) ||
                Objects.isNull(views) ||
                Objects.isNull(likes)) {
            log.warn("The record is invalid: {}. It will not be stored.", record);
            return false;
        }
        return true;
    }

    private static Long parseLongSafe(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate checkValidDate(String str, DateTimeFormatter formatter) {
        try {
            if (YearMonth.parse(str, formatter).isAfter(YearMonth.now())) {
                return null;
            }

            return YearMonth.parse(str, formatter).atDay(1);
        } catch (Exception e) {
            return null;
        }
    }

}
