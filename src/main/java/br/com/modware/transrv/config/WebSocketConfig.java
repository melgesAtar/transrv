package br.com.modware.transrv.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita um broker simples na memória para enviar mensagens aos clientes
        // Prefixo "/topic" para mensagens broadcast (todos os clientes)
        // Prefixo "/queue" para mensagens específicas (um cliente)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefixo para mensagens enviadas do cliente para o servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket que o cliente vai conectar
        // Permite CORS de qualquer origem (ajuste conforme necessário em produção)
        registry.addEndpoint("/ws/tickets")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Também suporta conexão nativa WebSocket (sem SockJS)
        registry.addEndpoint("/ws/tickets")
                .setAllowedOriginPatterns("*");
    }
}
