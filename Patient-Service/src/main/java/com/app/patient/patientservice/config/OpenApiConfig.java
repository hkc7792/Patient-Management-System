package com.app.patient.patientservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// SpringDoc OpenAPI 3 Configuration for Patient-Service API documentation.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI patientServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Patient Service API")
                        .description("RESTful microservice for managing patient records in the Patient Management System.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Patient Management System Engineering Team")
                                .email("support@patient-mgmt.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
