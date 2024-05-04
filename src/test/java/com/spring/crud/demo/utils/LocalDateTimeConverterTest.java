package com.spring.crud.demo.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

class LocalDateTimeConverterTest {
    private static LocalDateTimeConverter localDateTimeConverter;

    @BeforeAll
    static void beforeAll() {
        localDateTimeConverter = new LocalDateTimeConverter();
    }

    @Test
    void testGivenNull_WhenConvertToDatabaseColumn_ThenReturnNull() {
        // Given
        LocalDateTime expectedLocalDateTime = null;

        // When
        Timestamp timestamp = localDateTimeConverter.convertToDatabaseColumn(expectedLocalDateTime);

        // Then
        Assertions.assertNull(timestamp);
    }

    @Test
    void testGivenCurrentLocalDateTime_WhenConvertToDatabaseColumn_ThenReturnCurrentTimestamp() {
        // Given
        LocalDateTime expectedLocalDateTime = LocalDateTime.of(2024,1,1,12,12,12);

        // When
        Timestamp actualLocalDateTime = localDateTimeConverter.convertToDatabaseColumn(expectedLocalDateTime);

        // Then
        Assertions.assertNotNull(actualLocalDateTime);
        Assertions.assertEquals(expectedLocalDateTime,actualLocalDateTime.toLocalDateTime());
    }

    @Test
    void testGivenNull_WhenConvertToEntityAttribute_ThenReturnNull() {
        // Given
        Timestamp expectedLocalDateTime = null;

        // When
        LocalDateTime actualLocalDateTime = localDateTimeConverter.convertToEntityAttribute(expectedLocalDateTime);

        // Then
        Assertions.assertNull(actualLocalDateTime);
    }

    @Test
    void testGivenCurrentLocalDateTime_WhenConvertToEntityAttribute_ThenReturnCurrentTimestamp() {
        // Given
        Timestamp expectedLocalDateTime = Timestamp.valueOf(LocalDateTime.of(2024,1,1,12,12,12));

        // When
        LocalDateTime actualLocalDateTime = localDateTimeConverter.convertToEntityAttribute(expectedLocalDateTime);

        // Then
        Assertions.assertNotNull(actualLocalDateTime);
        Assertions.assertEquals(expectedLocalDateTime.toLocalDateTime(),actualLocalDateTime);
    }
}