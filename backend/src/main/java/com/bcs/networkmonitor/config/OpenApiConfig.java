package com.bcs.networkmonitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BCS Network Monitor API")
                        .version("1.0.0")
                        .description("REST API for registering network devices and tracking their operational status.")
                        .contact(new Contact()
                                .name("BCS Engineering")
                                .url("https://bcs.example.com")));
    }
}
