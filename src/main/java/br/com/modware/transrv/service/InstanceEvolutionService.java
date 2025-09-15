package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.evolution.sendMessage.SendPlainText;
import br.com.modware.transrv.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


@Service
public class InstanceEvolutionService {

    private final static String EVOLUTION_API_URL = "https://evolution1.modware.com.br";

    @Value("${evolution.api.key}")
    private String apiKey;

    static final ObjectMapper MAPPER = new ObjectMapper();

    private final Logger log = org.slf4j.LoggerFactory.getLogger(InstanceEvolutionService.class);


    public boolean sendMessageToPhone(String phoneNumber, String employeeNames, Ticket ticket, int level, WAGroup waGroup) {
        String colorEmoji;
        switch (level) {
            case 2 -> colorEmoji = "🟠"; // laranja
            case 3 -> colorEmoji = "🔴"; // vermelho
            default -> colorEmoji = "🟡"; // amarelo (nível 1)
        }

        String frame = colorEmoji.repeat(10);

        long minutesOpen = java.time.Duration
                .between(ticket.getCreatedAt(), java.time.LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo")))
                .toMinutes();

        String body = String.format(
                "*Novo Ticket Aberto*\n\n" +
                        "*Grupo:* %s\n" +
                        "*Alerta:* %s\n" +
                        "*Mensagem:* %s\n\n" +
                        "➡️ Responsáveis notificados: %s\n\n" +
                        "Para encerrar este chamado, responda a mensagem no grupo ou envie uma mensagem com o *ID* abaixo:\n\n" +
                        "*ID do Chamado:* %d\n" +
                        "*Nível de Prioridade:* %d\n" +
                        "*Tempo em aberto:* %d min",
                waGroup.getGroupName(),
                ticket.getAlertTerm().getCode(),
                ticket.getMessageResponsibleForOpeningTheCall().getMessageContent(),
                employeeNames,
                ticket.getId(),
                level,
                minutesOpen
        );

        String messageContent = frame + "\n" + body + "\n" + frame;

        SendPlainText sendPlainText = new SendPlainText();
        sendPlainText.setNumber(phoneNumber);
        sendPlainText.setText(messageContent);
        sendPlainText.setLinkPreview(true);

        try {
            sendMessage(sendPlainText);
            return true;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean sendMessageToGroup(WAGroup waGroup, Ticket ticket, int level) {
        String colorEmoji;
        switch (level) {
            case 2 -> colorEmoji = "🟠";
            case 3 -> colorEmoji = "🔴";
            default -> colorEmoji = "🟡";
        }

        String frame = colorEmoji.repeat(10);

        long minutesOpen = java.time.Duration
                .between(ticket.getCreatedAt(), java.time.LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo")))
                .toMinutes();

        String body = String.format(
                "*Alerta de Ticket em Aberto*\n\n" +
                        "*Grupo:* %s\n" +
                        "*Alerta:* %s\n" +
                        "*ID do Chamado:* %d\n" +
                        "*Nível de Prioridade:* %d\n" +
                        "*Tempo em aberto:* %d min\n\n" +
                        "Para encerrar, envie: 'ticket finalizado id %d'",
                waGroup.getGroupName(),
                ticket.getAlertTerm().getCode(),
                ticket.getId(),
                level,
                minutesOpen,
                ticket.getId()
        );

        String messageContent = frame + "\n" + body + "\n" + frame;

        SendPlainText sendPlainText = new SendPlainText();
        sendPlainText.setNumber(waGroup.getEvolutionGroupId());
        sendPlainText.setText(messageContent);
        sendPlainText.setLinkPreview(true);
        try {
            return sendMessage(sendPlainText);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendFinalizationToGroup(WAGroup waGroup, Ticket ticket) {
        if (waGroup == null) return false;
        String frame = "🟢".repeat(10);

        long minutesOpen = 0;
        try {
            java.time.LocalDateTime end = ticket.getClosedAt() != null ? ticket.getClosedAt() : java.time.LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"));
            minutesOpen = java.time.Duration.between(ticket.getCreatedAt(), end).toMinutes();
        } catch (Exception ignored) {}

        String body = String.format(
                "*Ticket Finalizado*\n\n" +
                        "*Grupo:* %s\n" +
                        "*Alerta:* %s\n" +
                        "*ID do Chamado:* %d\n" +
                        "*Tempo total em aberto:* %d min",
                waGroup.getGroupName(),
                ticket.getAlertTerm() != null ? ticket.getAlertTerm().getCode() : "-",
                ticket.getId(),
                minutesOpen
        );

        String messageContent = frame + "\n" + body + "\n" + frame;

        SendPlainText sendPlainText = new SendPlainText();
        sendPlainText.setNumber(waGroup.getEvolutionGroupId());
        sendPlainText.setText(messageContent);
        sendPlainText.setLinkPreview(true);
        try {
            return sendMessage(sendPlainText);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendMessage(SendPlainText sendPlainText) throws IOException, InterruptedException {
        String url = EVOLUTION_API_URL + "/message/sendText/" + "transRV";

        String json = MAPPER.writeValueAsString(sendPlainText);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("apikey", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response= httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 == 2) {
            log.debug("Mensagem enviada para Evolution | status={} bodyLen={}", response.statusCode(), response.body() != null ? response.body().length() : 0);
            return true;

        } else {
            log.warn("Falha ao enviar mensagem para Evolution | status={} body={} ", response.statusCode(), response.body());
            return false;
        }
    }

}
