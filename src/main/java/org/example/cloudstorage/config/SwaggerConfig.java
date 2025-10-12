package org.example.cloudstorage.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

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
@Configuration
public class SwaggerConfig {
}
