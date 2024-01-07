package com.spring.crud.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "openAPIConfig")
public class OpenAPIConfig {

    @Value(value = "${springdoc.version:unknown}")
    private String springDocVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("CRUD API")
                .version(springDocVersion)
                .contact(new Contact().name("Vijay Ankat").url("https://github.com/ankat"))
                .description("This is a sample CRUD application using spring data")
                .termsOfService("https://swagger.io/terms/")
                .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }

    /*
    @Bean
    @ConditionalOnMissingBean
    SwaggerWelcomeWebMvc swaggerWelcome(SwaggerUiConfigProperties swaggerUiConfig, SpringDocConfigProperties springDocConfigProperties, SwaggerUiConfigParameters swaggerUiConfigParameters, SpringWebProvider springWebProvider) {
        return new SwaggerWelcomeWebMvc(swaggerUiConfig, springDocConfigProperties, swaggerUiConfigParameters, springWebProvider);
    }
    */
}