package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.openai.AiClassifierResponse;
import br.com.modware.transrv.dto.openai.ResponseClassifierMessage;
import br.com.modware.transrv.exception.AgentNotFoundException;
import br.com.modware.transrv.exception.OpenAIException;
import br.com.modware.transrv.model.Agent;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class AiClassifier {

    @Value("${openai.api.key}")
    private String openAiApiKey;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final AgentService agentService;
    private final AlertTermsService alertTermsService;

    public AiClassifier(AgentService agentService, AlertTermsService alertTermsService) {
        this.agentService = agentService;
        this.alertTermsService = alertTermsService;
    }

    public ResponseClassifierMessage ticketClassification(String messageContent) {
        try {
            Optional<Agent> agentClassificationMessages  = agentService.findByName("Transportadora - Classificação de Mensagens");
            if (agentClassificationMessages.isEmpty()) {
                throw new AgentNotFoundException("Agent 'Transportadora - Classificação de Mensagens' not found");
            }
            String prompt = agentClassificationMessages.get().getPrompt() + "\n" +
                    alertTermsService.findAllActiveAlertTerms()
                            .stream()
                            .map(term -> term.getCode() + " - " + term.getDescription())
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");

            String requestBody = """
{
  "model": "gpt-4o-mini",
  "temperature": 0.1,
  "max_tokens": 150,
  "response_format": { "type": "json_object" },
  "messages": [
    { "role": "system", "content": %s },
    { "role": "user",   "content": %s }
  ]
}
""".formatted(toJsonString(prompt), toJsonString(messageContent));


            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            // Log menos poluído: apenas status e tamanho da resposta
            org.slf4j.LoggerFactory.getLogger(AiClassifier.class)
                    .debug("OpenAI status={} bodyLen={}", response.statusCode(), body != null ? body.length() : 0);

            Gson gson = new Gson();

            if (response.statusCode() / 100 != 2) {
                throw new OpenAIException("OpenAI 4xx/5xx: status=" + response.statusCode());
            }

            return gson.fromJson(body, ResponseClassifierMessage.class);

        } catch (Exception e) {
            throw new OpenAIException("Falha ao classificar mensagem com OpenAI: " + e.getMessage());
        }
    }

    private static String toJsonString(String s) {
        return new Gson().toJson(s);
    }

}
