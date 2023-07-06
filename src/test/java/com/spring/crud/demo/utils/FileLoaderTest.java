package com.spring.crud.demo.utils;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class FileLoaderTest {

    @Test
    void testGivenFileName_WhenGetFileFromResource_ThenReturnFile(){
        // Given
        String fileName = "test.txt";

        // When
        File file = FileLoader.getFileFromResource(fileName);

        // Then
        Assertions.assertThat(file).isNotNull();
        Assertions.assertThat(file).isFile();
        Assertions.assertThat(file).isNotEmpty();
    }

    @Test
    void testGivenRandomFileName_WhenGetFileFromResource_ThenReturnError(){
        // Given
        String fileName = "random.json";

        // When & Then
        Assertions.assertThatThrownBy(() -> FileLoader.getFileFromResource(fileName))
                .isInstanceOf(RuntimeException.class);

    }

    @Test
    void testGivenFile_WhenReadFromFile_ThenReturnString(){
        // Given
        File file = FileLoader.getFileFromResource("test.txt");

        // When
        String str = FileLoader.readFromFile(file);

        // Then
        Assertions.assertThat(str).isNotNull();
        Assertions.assertThat(str).isNotEmpty();
        Assertions.assertThat(str).isEqualTo("Hello World");
    }

    @Test
    void testGivenMockFile_WhenReadFromFile_ThenReturnError(){
        // Given
        File file = FileLoader.getFileFromResource("test.txt");

        // When & Then
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.readAllBytes(Paths.get(file.getAbsolutePath()))).thenThrow(IOException.class);
            Assertions.assertThatThrownBy(() -> FileLoader.readFromFile(file))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}