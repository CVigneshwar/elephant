package com.highschool.scheduler.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter[] SUPPORTED_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    @Override
    public String convertToDatabaseColumn(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isEmpty()) return null;

        // Try all supported formats
        for (DateTimeFormatter fmt : SUPPORTED_FORMATS) {
            try {
                // Trim trailing fractional seconds if any
                String clean = dbValue.split("\\.")[0];
                return LocalDate.parse(clean.trim(), fmt);
            } catch (Exception ignore) {
            }
        }

        throw new RuntimeException("Unparseable LocalDate: " + dbValue);
    }
}

