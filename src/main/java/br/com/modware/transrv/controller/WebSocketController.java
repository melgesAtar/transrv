package br.com.modware.transrv.controller;

import br.com.modware.transrv.dto.ticket.TicketEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
@Tag(name = "WebSocket", description = "Documentação sobre conexão WebSocket para notificações em tempo real de tickets")
public class WebSocketController {

    @Operation(
        summary = "Informações sobre WebSocket",
        description = """
            Retorna informações sobre como conectar ao WebSocket para receber notificações em tempo real.
            
            **Endpoint WebSocket:**
            - `ws://localhost:8080/ws/tickets` (WebSocket nativo)
            - `http://localhost:8080/ws/tickets` (com SockJS fallback)
            
            **Tópicos disponíveis:**
            - `/topic/tickets` - Recebe todos os eventos de tickets
            - `/topic/tickets/alerts` - Recebe apenas alertas críticos (escalação nível 3)
            
            **Tipos de eventos:**
            - `TICKET_OPENED` - Novo ticket aberto
            - `TICKET_CLOSED` - Ticket fechado
            - `TICKET_UPDATED` - Ticket atualizado (mudança de nível)
            - `TICKET_ESCALATED` - Ticket escalado
            - `TICKET_MARKED_INCORRECT` - Ticket marcado como incorreto
            
            **Exemplo de conexão JavaScript:**
            ```javascript
            import SockJS from 'sockjs-client';
            import { Stomp } from '@stomp/stompjs';
            
            const socket = new SockJS('http://localhost:8080/ws/tickets');
            const stompClient = Stomp.over(socket);
            
            stompClient.connect({}, function(frame) {
                stompClient.subscribe('/topic/tickets', function(message) {
                    const event = JSON.parse(message.body);
                    console.log('Evento:', event);
                });
            });
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informações sobre WebSocket",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getWebSocketInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("endpoint", "ws://localhost:8080/ws/tickets");
        info.put("endpointSockJS", "http://localhost:8080/ws/tickets");
        info.put("protocol", "STOMP over WebSocket");
        
        Map<String, String> topics = new HashMap<>();
        topics.put("/topic/tickets", "Todos os eventos de tickets");
        topics.put("/topic/tickets/alerts", "Apenas alertas críticos (escalação nível 3)");
        info.put("topics", topics);
        
        Map<String, String> eventTypes = new HashMap<>();
        eventTypes.put("TICKET_OPENED", "Novo ticket aberto");
        eventTypes.put("TICKET_CLOSED", "Ticket fechado");
        eventTypes.put("TICKET_UPDATED", "Ticket atualizado (mudança de nível)");
        eventTypes.put("TICKET_ESCALATED", "Ticket escalado");
        eventTypes.put("TICKET_MARKED_INCORRECT", "Ticket marcado como incorreto");
        info.put("eventTypes", eventTypes);
        
        Map<String, Object> exampleTicket = new HashMap<>();
        exampleTicket.put("id", 123L);
        exampleTicket.put("currentEscalationLevel", 1);
        exampleTicket.put("status", "OPEN");
        exampleTicket.put("groupName", "Grupo WhatsApp");
        exampleTicket.put("alertTermCode", "RISCO_ETA_ORIGEM");
        
        Map<String, Object> exampleEvent = new HashMap<>();
        exampleEvent.put("type", "TICKET_OPENED");
        exampleEvent.put("ticket", exampleTicket);
        exampleEvent.put("timestamp", "2024-01-26T22:30:00");
        info.put("exampleEvent", exampleEvent);
        
        Map<String, String> dependencies = new HashMap<>();
        dependencies.put("npm", "npm install sockjs-client @stomp/stompjs");
        dependencies.put("yarn", "yarn add sockjs-client @stomp/stompjs");
        info.put("dependencies", dependencies);
        
        return ResponseEntity.ok(info);
    }
    
    @Operation(
        summary = "Exemplo de evento TicketEvent",
        description = "Retorna um exemplo do formato de evento que será recebido via WebSocket"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exemplo de evento",
            content = @Content(schema = @Schema(implementation = TicketEvent.class)))
    })
    @GetMapping("/example-event")
    public ResponseEntity<Map<String, Object>> getExampleEvent() {
        Map<String, Object> example = new HashMap<>();
        example.put("type", "TICKET_OPENED");
        
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("id", 123L);
        ticket.put("currentEscalationLevel", 1);
        ticket.put("groupName", "Grupo WhatsApp");
        ticket.put("groupEvolutionId", "1234567890@g.us");
        ticket.put("alertTermCode", "RISCO_ETA_ORIGEM");
        ticket.put("alertTermName", "Risco ETA Origem");
        ticket.put("alertTermDescription", "Descrição do alerta");
        ticket.put("status", "OPEN");
        ticket.put("createdAt", "2024-01-26T22:30:00");
        ticket.put("closedAt", null);
        ticket.put("openingMessageContent", "Conteúdo da mensagem");
        ticket.put("contactName", "Nome do Contato");
        ticket.put("contactPhone", "5511999999999");
        ticket.put("employeeName", "Nome do Funcionário");
        ticket.put("openedIncorrectly", false);
        
        example.put("ticket", ticket);
        example.put("timestamp", "2024-01-26T22:30:00");
        
        return ResponseEntity.ok(example);
    }
}
