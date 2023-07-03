package com.spring.crud.demo.controller.mock;

import com.spring.crud.demo.controller.DefaultController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class DefaultControllerTest {

    @Autowired
    private DefaultController defaultController;

    @Test
    void redirect() {
        // Given & When
        ResponseEntity<Void> response = defaultController.redirect();

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    }

    @Test
    void databaseUrl() {
        // Given & When
        ResponseEntity<Void> response = defaultController.databaseUrl();

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    }
}