package com.spring.crud.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController(value = "defaultController")
public class DefaultController {

    @Value(value = "${springdoc.swagger-ui.path:swagger-ui-custom.html}")
    private String swaggerUIPath;

    @Operation(summary = "Swagger documentation url", hidden = true)
    @GetMapping
    public ResponseEntity<Void> redirect() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(swaggerUIPath))
                .build();
    }

    @Operation(summary = "Database url", hidden = true)
    @GetMapping("/database")
    public ResponseEntity<Void> databaseUrl() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("h2-console"))
                .build();

    }

}
