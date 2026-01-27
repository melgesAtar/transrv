package br.com.modware.transrv.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TransRV API")
                        .version("1.0.0")
                        .description("""
                            API REST para gerenciamento de tickets e chamados do sistema TransRV.
                            
                            **WebSocket para Notificações em Tempo Real:**
                            - Endpoint: `ws://localhost:8080/ws/tickets`
                            - Protocolo: STOMP over WebSocket
                            - Tópicos: `/topic/tickets` e `/topic/tickets/alerts`
                            - Veja mais detalhes em `/api/websocket/info`
                            """)
                        .contact(new Contact()
                                .name("TransRV Team")
                                .email("suporte@transrv.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("jwt")
                                .description("JWT token armazenado em cookie HttpOnly")));
    }
}
