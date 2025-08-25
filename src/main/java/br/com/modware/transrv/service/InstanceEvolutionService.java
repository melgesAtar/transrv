package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.evolution.sendMessage.SendPlainText;
import br.com.modware.transrv.model.*;
import br.com.modware.transrv.repository.InstanceEvolutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class InstanceEvolutionService {
    private final static String EVOLUTION_API_URL = "https://evolution1.modware.com.br";
    @Value("${evolution.api.key}")
    private String apiKey;
    static final ObjectMapper MAPPER = new ObjectMapper();

    private final InstanceEvolutionRepository instanceEvolutionRepository;

    public InstanceEvolutionService(InstanceEvolutionRepository instanceEvolutionRepository) {
        this.instanceEvolutionRepository = instanceEvolutionRepository;
    }

    public boolean existsByInstanceName(String instanceName){
        return instanceEvolutionRepository.existsByInstanceName(instanceName);
    }

    List<InstanceEvolution> findAll() {
        return instanceEvolutionRepository.findAll();
    }

    public void sendMessageToEmployee(WAGroup waGroup, Employee employee, Ticket ticket , int level) {
        String messageContent = String.format("Novo ticket aberto no grupo %s, ALERTA: %s, conteúdo da mensagem: %s", waGroup.getGroupName(), ticket.getAlertTerm(), ticket.getMessageResponsibleForOpeningTheCall().getMessageContent() + "encerre esse chamado respondendo a mensagem no grupo, ou enviando uma mensagem contendo o ID do chamado no grupo \n\n" + "NIVEL DE PRIORIDADE MENSAGEM: " + level);
        SendPlainText sendPlainText = new SendPlainText();
        sendPlainText.setNumber(employee.getWaContact().getPhoneNumber());
        sendPlainText.setText(messageContent);
        sendPlainText.setLinkPreview(true);
        try {
            sendMessage(sendPlainText);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
    private void sendMessage(SendPlainText sendPlainText) throws IOException, InterruptedException {
        String json = MAPPER.writeValueAsString(sendPlainText);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EVOLUTION_API_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("apikey", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response= httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 == 2) {
            System.out.println("OK: " + response.body());

        } else {
            throw new RuntimeException("Falha: " + response.statusCode() + " -> " + response.body());
        }
    }

}
