package com.tedtalks.assignment.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@UtilityClass
public class FileUtil {

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
