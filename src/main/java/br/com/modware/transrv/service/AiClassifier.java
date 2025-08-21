package br.com.modware.transrv.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class AiClassifier {
    @Value("${openai.api.key}")
    private String openAiApiKey;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public String ticketClassification(String messageContent) {
        try {

            String prompt = "Você está analisando mensagens de um grupo de WhatsApp de uma transportadora. "
                    + "Sua tarefa é decidir se a mensagem precisa de abertura de ticket, isto é, se alguém precisa responder aquela mensagem ou não.\n"
                    + "- Se a mensagem não requer resposta → devolva \"should_open\": false.\n"
                    + "- Se a mensagem requer resposta → devolva \"should_open\": true e informe qual termo de alerta da lista se encaixa.\n"
                    + "- Caso nenhum termo se aplique, devolva \"should_open\": true com \"alert_term\": \"outro\" apenas se a mensagem ainda assim parecer que precisa de ação.\n"
                    + "- Saída sempre em JSON válido.\n\n"
                    + "Lista de termos de alerta:\n"
                    + "- Risco ETA ORIGEM\n- Perda ETA ORIGEM\n- Perda ETA Destino\n- Notificação motoristas ->48hr\n"
                    + "- Problema mecânico\n- Acidente\n- Descarga / descarregar\n- Ocorrência\n- Defesa\n- No Show / No show\n"
                    + "- 5 por ques\n- 5W2H\n- Quebra de PGR\n- Raster\n- T4S\n- Veículo bloqueado\n- Carreta carregada\n"
                    + "- Infrutífero\n- Liberado vazio\n- Minuta divergente\n- Minuta errada\n- Risco ETA Destino\n"
                    + "- Risco de impacto\n- Rota cancelada\n- Vrid cancelado";

            String requestBody = """
{
  "model": "gpt-4o-mini",
  "temperature": 0.1,
  "messages": [
    {
      "role": "system",
      "content": "Você está analisando mensagens de um grupo de WhatsApp de uma transportadora. Sua tarefa é decidir se a mensagem precisa de abertura de ticket, isto é, se alguém precisa responder aquela mensagem ou não. Se a mensagem não requer resposta devolva {\\\"should_open\\\": false}. Se a mensagem requer resposta devolva {\\\"should_open\\\": true, \\\"alert_term\\\": \\\"<termo da lista>\\\"}. Lista de termos: Risco ETA ORIGEM, Perda ETA ORIGEM, Perda ETA Destino, Notificação motoristas ->48hr, Problema mecânico, Acidente, Descarga / descarregar, Ocorrência, Defesa, No Show, 5 por ques, 5W2H, Quebra de PGR, Raster, T4S, Veículo bloqueado, Carreta carregada, Infrutífero, Liberado vazio, Minuta divergente, Minuta errada, Risco ETA Destino, Risco de impacto, Rota cancelada, Vrid cancelado."
    },
    {
      "role": "user",
      "content": "%s"
    }
  ]
}
""".formatted(messageContent.replace("\"", "\\\""));


            // Criar cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Erro ao classificar mensagem\"}";
        }
    }
}
