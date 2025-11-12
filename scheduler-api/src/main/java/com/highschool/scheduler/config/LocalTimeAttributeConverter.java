package com.highschool.scheduler.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;

@Converter(autoApply = true)
public class LocalTimeAttributeConverter implements AttributeConverter<LocalTime, String> {

    @Override
    public String convertToDatabaseColumn(LocalTime time) {
        return time != null ? time.toString() : null; // stores as "HH:mm:ss"
    }

    @Override
    public LocalTime convertToEntityAttribute(String value) {
        return (value != null && !value.isEmpty()) ? LocalTime.parse(value) : null;
    }
}
