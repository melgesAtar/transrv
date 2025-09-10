package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.evolution.GroupInfo;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class EvolutionApiClient {

    @Value("${evolution.api.key}")
    private String apiKey;

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GroupInfo fetchGroupInfo(String serverUrl, String instanceName, String groupJid) {
        try {
            String safeServer = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
            String encodedJid = URLEncoder.encode(groupJid, StandardCharsets.UTF_8);
            String url = safeServer + "/group/findGroupInfos/" + instanceName + "?groupJid=" + encodedJid;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Evolution group info falhou: status=" + response.statusCode());
            }
            return gson.fromJson(response.body(), GroupInfo.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar informações do grupo: " + e.getMessage(), e);
        }
    }
}


