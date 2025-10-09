package org.example.cloudstorage.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Cloud storage",
                description = "Multi-user file cloud like Google Drive",
                contact = @Contact(
                        name = "Kuznetsov Dmitry",
                        url = "https://github.com/ProgWrite"
                )
        )
)
public class OpenAPIConfig {
}
