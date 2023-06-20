package com.spring.crud.demo.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
public class FileLoader {

    private FileLoader() {
    }

    public static File getFileFromResource(String fileName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return Paths.get(Objects.requireNonNull(classLoader.getResource(fileName)).toURI()).toFile();
        } catch (URISyntaxException e) {
            log.error("Failed to load file: '{}'. Check if the file exists and is placed in the 'resource' directory", fileName);
            throw new RuntimeException(e);
        }
    }

    public static String readFromFile(File file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        } catch (IOException ioe) {
            log.error("Failed to read file and convert to string : ", ioe);
            throw new RuntimeException(ioe);
        }
    }
}
