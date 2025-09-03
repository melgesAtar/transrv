package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.evolution.EventEvolution;
import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.model.WAConversation;
import br.com.modware.transrv.model.WAMessage;
import br.com.modware.transrv.repository.WAMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WAMessageService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private static final String BASE_URL = "https://api.openai.com/v1";
    private static final String MODEL_VISION = "gpt-4.1-mini";
    private static final String MODEL_TRANSCRIBE = "gpt-4o-transcribe";
    private static final long MAX_DOC_BYTES = 5L * 1024 * 1024; // 3 MiB
    private final ObjectMapper mapper = new ObjectMapper();
    private final WAMessageRepository waMessageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final ConcurrentHashMap<String, Object> conversationLocks = new ConcurrentHashMap<>();

    public WAMessageService(WAMessageRepository waMessageRepository) {
        this.waMessageRepository = waMessageRepository;
    }


    public WAMessage processMessage(EventEvolution eventEvolution, WAConversation waConversation, WAContact waContact) {
        String convKey = eventEvolution.getData().getKey().getRemoteJid();
        Object lock = conversationLocks.computeIfAbsent(convKey, k -> new Object());

        synchronized (lock) {
            return waMessageRepository.findByEvolutionMessageId(eventEvolution.getData().getKey().getId())
                    .orElseGet(() -> {
                        String type = eventEvolution.getData().getMessageType();

                        String contentMessage;
                        switch (type) {
                            case "conversation" -> contentMessage = processTextMessage(eventEvolution);
                            case "audioMessage" -> contentMessage = processAudioMessage(eventEvolution);
                            case "imageMessage" -> contentMessage = processImageMessage(eventEvolution);
                            case "documentMessage" -> contentMessage = processDocumentMessage(eventEvolution);
                            case "videoMessage" -> contentMessage = "USUARIO ENVIOU UM VÍDEO";
                            case "stickerMessage" -> contentMessage = "USUARIO ENVIOU UM STICKER";
                            case "locationMessage" -> contentMessage = "USUARIO ENVIOU UMA LOCALIZAÇÃO";
                            case "contactMessage" -> contentMessage = "USUARIO ENVIOU UM CONTATO";
                            case "pollCreationMessage" -> contentMessage = "USUARIO CRIOU UMA ENQUETE";
                            default -> contentMessage = null;
                        }

                        WAMessage newMessage = new WAMessage();
                        newMessage.setWaConversation(waConversation);
                        newMessage.setEvolutionMessageId(eventEvolution.getData().getKey().getId());
                        newMessage.setSender(waContact);
                        newMessage.setMessageContent(contentMessage);
                        newMessage.setSentAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));

                        WAMessage saved = waMessageRepository.save(newMessage);
                        return saved;
                    });
        }
    }



    // ======== TIPOS ========

    private String processTextMessage(EventEvolution eventEvolution) {
        return eventEvolution.getData().getMessage().getConversation();
    }

    private String processAudioMessage(EventEvolution eventEvolution) {
        File audioFile = base64ToFile(eventEvolution, "audioMessage");
        String transcription = getAudioTranscription(audioFile);
        if (transcription == null || transcription.isEmpty()) {
            throw new IllegalStateException("Transcription vazia para áudio ID: " + eventEvolution.getData().getKey().getId());
        }
        return transcription;
    }

    private String processImageMessage(EventEvolution eventEvolution) {
        File imageFile = base64ToFile(eventEvolution, "imageMessage");
        String contentMessage =  getImageDescription(imageFile);
        String caption = (eventEvolution.getData().getMessage().getImageMessage() != null)
                ? eventEvolution.getData().getMessage().getImageMessage().getCaption()
                : null;
        if (caption != null && !caption.isEmpty()) {
            contentMessage = (contentMessage != null && !contentMessage.isEmpty())
                    ? contentMessage + "\n" + caption
                    : caption;
        }
        return contentMessage;
    }

    // ======== AUX ========

    private String getAudioTranscription(File audioFile) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String apiUrl = BASE_URL + "/audio/transcriptions";

            String boundary = "----JavaFormBoundary" + System.currentTimeMillis();
            String LF = "\r\n";

            byte[] fileBytes = Files.readAllBytes(audioFile.toPath());

            String detected = Files.probeContentType(audioFile.toPath());
            String audioMime = (detected != null && !detected.isBlank()) ? detected : "application/octet-stream";

            StringBuilder sb = new StringBuilder();
            sb.append("--").append(boundary).append(LF);
            sb.append("Content-Disposition: form-data; name=\"model\"").append(LF).append(LF);
            sb.append(MODEL_TRANSCRIBE).append(LF);

            sb.append("--").append(boundary).append(LF);
            sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(audioFile.getName()).append("\"").append(LF);
            sb.append("Content-Type: ").append(audioMime).append(LF).append(LF);

            byte[] prefix = sb.toString().getBytes();
            byte[] suffix = (LF + "--" + boundary + "--" + LF).getBytes();

            byte[] requestBody = new byte[prefix.length + fileBytes.length + suffix.length];
            System.arraycopy(prefix, 0, requestBody, 0, prefix.length);
            System.arraycopy(fileBytes, 0, requestBody, prefix.length, fileBytes.length);
            System.arraycopy(suffix, 0, requestBody, prefix.length + fileBytes.length, suffix.length);

            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(apiUrl))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();

            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Transcribe falhou: " + response.statusCode() + " - " + response.body());
            }

            JsonNode root = mapper.readTree(response.body());
            return root.path("text").asText(null);

        } catch (Exception e) {
            throw new RuntimeException("Erro na transcrição: " + e.getMessage(), e);
        }
    }

    private String getImageDescription(File imageFile) {

        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key não configurada.");
        }

        try {
            String detected = Files.probeContentType(imageFile.toPath());
            String mime = (detected != null) ? detected : "image/jpeg";
            String ext = switch (mime) {
                case "image/jpeg", "image/jpg" -> "jpg";
                case "image/png" -> "png";
                case "image/gif" -> "gif";
                case "image/webp" -> "webp";
                default -> "jpg";
            };
            String goodFilename = "image." + ext;
            byte[] bytes = Files.readAllBytes(imageFile.toPath());

            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.setBearerAuth(openaiApiKey);
            uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("purpose", "vision");

            ByteArrayResource filePart = new ByteArrayResource(bytes) {
                @Override public String getFilename() { return goodFilename; }
            };
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(mime));
            HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(filePart, fileHeaders);
            body.add("file", fileEntity);

            HttpEntity<MultiValueMap<String, Object>> uploadRequest = new HttpEntity<>(body, uploadHeaders);
            ResponseEntity<String> uploadResponse = restTemplate.postForEntity(
                    BASE_URL + "/files", uploadRequest, String.class);

            if (!uploadResponse.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Upload falhou: " + uploadResponse.getStatusCode() + " - " + uploadResponse.getBody());
            }

            String fileId = mapper.readTree(uploadResponse.getBody()).path("id").asText();
            if (fileId == null || fileId.isBlank()) {
                throw new IllegalStateException("Upload OK, mas não retornou id: " + uploadResponse.getBody());
            }

            String payload = """
            {
              "model": "%s",
              "input": [
                {
                  "role": "user",
                  "content": [
                    { "type": "input_text", "text": "o que tem nessa imagem?" },
                    { "type": "input_image", "file_id": "%s" }
                  ]
                }
              ]
            }
            """.formatted(MODEL_VISION, fileId);

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setBearerAuth(openaiApiKey);
            respHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> respRequest = new HttpEntity<>(payload, respHeaders);
            ResponseEntity<String> resp = restTemplate.postForEntity(BASE_URL + "/responses", respRequest, String.class);

            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Responses falhou: " + resp.getStatusCode() + " - " + resp.getBody());
            }

            JsonNode json = mapper.readTree(resp.getBody());

            JsonNode convenience = json.get("output_text");
            if (convenience != null && !convenience.isNull()) {
                return convenience.asText();
            }

            StringBuilder sb = new StringBuilder();
            JsonNode outputArr = json.path("output");
            if (outputArr.isArray()) {
                for (JsonNode msg : outputArr) {
                    JsonNode contentArr = msg.path("content");
                    if (contentArr.isArray()) {
                        for (JsonNode c : contentArr) {
                            if ("output_text".equals(c.path("type").asText())) {
                                String t = c.path("text").asText(null);
                                if (t != null && !t.isBlank()) {
                                    if (sb.length() > 0) sb.append("\n");
                                    sb.append(t);
                                }
                            }
                        }
                    }
                }
            }
            return sb.length() > 0 ? sb.toString() : resp.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Erro no processamento da imagem: " + e.getMessage(), e);
        }
    }



    private String extractText(File file) {
        try (java.io.InputStream is = Files.newInputStream(file.toPath())) {
            org.apache.tika.Tika tika = new org.apache.tika.Tika();
            // Limita para evitar textos absurdamente grandes
            tika.setMaxStringLength(2_000_000); // ~2M chars
            return tika.parseToString(is);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao extrair texto com Tika: " + e.getMessage(), e);
        }
    }


    private String summarizeWithOpenAI(String userText) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(openaiApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Prompt simples e direto
            String system = "Você é um assistente que resume documentos de forma clara e objetiva em português.";
            String user = "Resuma em 5-7 bullets e cite seções/abas quando houver.\n\nConteúdo:\n" + userText;

            // Limitar texto para evitar estouro de tokens (ajuste se quiser)
            if (user.length() > 200_000) {
                user = user.substring(0, 200_000) + "\n\n[...conteúdo truncado...]";
            }

            // Monta payload do chat/completions
            String payload = """
        {
          "model": "gpt-4o-mini",
          "temperature": 0.2,
          "max_tokens": 400,
          "messages": [
            { "role": "system", "content": %s },
            { "role": "user",   "content": %s }
          ]
        }
        """.formatted(mapper.writeValueAsString(system), mapper.writeValueAsString(user));

            ResponseEntity<String> resp = restTemplate.postForEntity(
                    BASE_URL + "/chat/completions",
                    new HttpEntity<>(payload, headers),
                    String.class
            );

            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("OpenAI retornou " + resp.getStatusCode() + ": " + resp.getBody());
            }

            JsonNode root = mapper.readTree(resp.getBody());
            JsonNode choice0 = root.path("choices").path(0).path("message").path("content");
            String out = choice0.isMissingNode() ? null : choice0.asText();
            return (out != null && !out.isBlank()) ? out : "Não foi possível gerar resumo.";
        } catch (Exception e) {
            throw new RuntimeException("Erro no resumo com OpenAI: " + e.getMessage(), e);
        }
    }


    private String processDocumentMessage(EventEvolution eventEvolution) {

        File docFile = base64ToFile(eventEvolution, "documentMessage");

        try {
            long size = Files.size(docFile.toPath());
            if (size > 5L * 1024 * 1024) { // 5 MiB
                return "USUARIO ENVIOU UM ARQUIVO COM MAIS DE 5MB";
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível verificar o tamanho do arquivo: " + e.getMessage(), e);
        }

        // 1) Extrai texto localmente (SEM mandar arquivo para a OpenAI)
        String text = extractText(docFile);
        if (text == null || text.isBlank()) {
            return "Não consegui extrair texto do arquivo (talvez esteja vazio ou protegido).";
        }

        // 2) Manda só TEXTO para o chat/completions e pega o resumo
        return summarizeWithOpenAI(text);
    }



    private File base64ToFile(EventEvolution eventEvolution, String typeMessage) {
        String base64 = eventEvolution.getData().getMessage().getBase64();
        String mime = switch (typeMessage) {
            case "audioMessage" -> eventEvolution.getData().getMessage().getAudioMessage().getMimetype();
            case "imageMessage" -> eventEvolution.getData().getMessage().getImageMessage().getMimetype();
            case "documentMessage" -> eventEvolution.getData().getMessage().getDocumentMessage().getMimeType();
            default -> null;
        };

        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(base64);
            String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS").format(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));

            String ext = (mime != null && mime.contains("ogg")) ? ".ogg"
                    : (mime != null && mime.contains("mp3")) ? ".mp3"
                    : (mime != null && mime.contains("m4a")) ? ".m4a"
                    : (mime != null && mime.contains("aac")) ? ".aac"
                    : (mime != null && mime.contains("wav")) ? ".wav"
                    : (mime != null && mime.contains("flac")) ? ".flac"
                    : (mime != null && mime.contains("jpeg")) ? ".jpeg"
                    : (mime != null && mime.contains("jpg")) ? ".jpg"
                    : (mime != null && mime.contains("png")) ? ".png"
                    : (mime != null && mime.contains("gif")) ? ".gif"
                    : (mime != null && mime.contains("webp")) ? ".webp"
                    : (mime != null && mime.contains("xlsx")) ? ".xlsx"
                    : (mime != null && mime.contains("xls")) ? ".xls"
                    : (mime != null && mime.contains("csv")) ? ".csv"
                    : (mime != null && mime.contains("pdf")) ? ".pdf"
                    : ".tmp";

            Path tmpPath = Files.createTempFile("file_" + ts + "_", ext);
            File file = tmpPath.toFile();
            file.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(decoded);
            }
            return file;

        } catch (IOException e) {
            throw new RuntimeException("Erro base64->file: " + e.getMessage(), e);
        }
    }

    public WAMessage saveMessage(WAMessage waMessage) {
        return waMessageRepository.save(waMessage);
}

}
