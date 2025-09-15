package br.com.modware.transrv.service;

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

@Service
public class AiClassifier {

    @Value("${openai.api.key}")
    private String openAiApiKey;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final AlertTermsService alertTermsService;

    public AiClassifier(AlertTermsService alertTermsService) {
        this.alertTermsService = alertTermsService;
    }

    public ResponseClassifierMessage ticketClassification(String messageContent, Agent agent) {
        try {
            if (messageContent == null || messageContent.isBlank()) {
                throw new OpenAIException("Conteúdo da mensagem vazio para classificação");
            }

            if (agent == null || agent.getPrompt() == null || agent.getPrompt().isBlank()) {
                throw new AgentNotFoundException("Agente do grupo não configurado ou sem prompt");
            }

            String prompt = agent.getPrompt() + "\n" +
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
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AiClassifier.class);
            logger.debug("OpenAI status={} bodyLen={}", response.statusCode(), body != null ? body.length() : 0);

            Gson gson = new Gson();

            if (response.statusCode() / 100 != 2) {
                // Logar o corpo para diagnosticar 400/422/etc
                logger.error("OpenAI error status={} body={}", response.statusCode(), body);

                // Fallback: se 400, tentar sem response_format (alguns modelos rejeitam)
                if (response.statusCode() == 400) {
                    String fallbackBody = ("{\n" +
                            "  \"model\": \"gpt-4o-mini\",\n" +
                            "  \"temperature\": 0.1,\n" +
                            "  \"max_tokens\": 150,\n" +
                            "  \"messages\": [\n" +
                            "    { \"role\": \"system\", \"content\": %s },\n" +
                            "    { \"role\": \"user\",   \"content\": %s }\n" +
                            "  ]\n" +
                            "}\n").formatted(toJsonString(prompt), toJsonString(messageContent));

                    HttpRequest fallbackReq = HttpRequest.newBuilder()
                            .uri(URI.create(API_URL))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + openAiApiKey)
                            .POST(HttpRequest.BodyPublishers.ofString(fallbackBody, StandardCharsets.UTF_8))
                            .build();

                    HttpResponse<String> fbRes = client.send(fallbackReq, HttpResponse.BodyHandlers.ofString());
                    if (fbRes.statusCode() / 100 == 2) {
                        logger.warn("OpenAI 400 com response_format; fallback sem response_format funcionou");
                        return gson.fromJson(fbRes.body(), ResponseClassifierMessage.class);
                    } else {
                        logger.error("OpenAI fallback também falhou | status={} body={} ", fbRes.statusCode(), fbRes.body());
                    }
                }

                throw new OpenAIException("OpenAI 4xx/5xx: status=" + response.statusCode() + " body=" + body);
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
