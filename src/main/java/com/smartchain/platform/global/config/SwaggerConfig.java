package com.smartchain.platform.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ESG 실사 자동화 플랫폼 API")
                        .description("AI 기반 공급망 ESG 실사 자동화 플랫폼")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("AIVLE 10조")
                                .email("team10@example.com"))
                );
    }
}
