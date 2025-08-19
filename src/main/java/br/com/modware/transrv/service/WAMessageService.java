package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.EventEvolution;
import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.model.WAConversation;
import br.com.modware.transrv.model.WAMessage;
import br.com.modware.transrv.repository.WAMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private static final ZoneId TZ = ZoneId.of("America/Sao_Paulo");

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
            waMessageRepository.findByEvolutionMessageId(eventEvolution.getData().getKey().getId())
                    .ifPresentOrElse(existingMessage -> {}, () -> {
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
                        newMessage.setSentAt(LocalDateTime.now(TZ));

                        waMessageRepository.save(newMessage);
                    });
        }
        return null;
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

    private String processDocumentMessage(EventEvolution eventEvolution) {
        // Converte o base64 em arquivo temporário
        File docFile = base64ToFile(eventEvolution, "documentMessage");

        // ✅ Limite de 3 MB
        try {
            long size = Files.size(docFile.toPath());
            if (size > MAX_DOC_BYTES) {
                return "USUARIO ENVIOU UM ARQUIVO COM MAIS DE 3MB";
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível verificar o tamanho do arquivo: " + e.getMessage(), e);
        }

        try {
            // Detecta MIME do evento ou do arquivo
            String mimeFromEvent = (eventEvolution.getData().getMessage().getDocumentMessage() != null)
                    ? eventEvolution.getData().getMessage().getDocumentMessage().getMimeType()
                    : null;
            String detectedMime = (mimeFromEvent != null && !mimeFromEvent.isBlank())
                    ? mimeFromEvent
                    : Files.probeContentType(docFile.toPath());
            if (detectedMime == null || detectedMime.isBlank()) detectedMime = "application/pdf";

            // Extensão amigável para o upload
            String ext = switch (detectedMime) {
                case "application/pdf" -> "pdf";
                case "application/msword" -> "doc";
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
                case "application/vnd.ms-powerpoint" -> "ppt";
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "pptx";
                case "application/vnd.ms-excel" -> "xls";
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx";
                case "text/plain" -> "txt";
                case "text/csv" -> "csv";
                default -> "pdf";
            };
            String goodFilename = "document." + ext;

            byte[] bytes = Files.readAllBytes(docFile.toPath());

            // 1) Upload na Files API
            HttpHeaders upHeaders = new HttpHeaders();
            upHeaders.setBearerAuth(openaiApiKey);
            upHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            var body = new LinkedMultiValueMap<String, Object>();
            body.add("purpose", "assistants");

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(detectedMime));

            ByteArrayResource filePart = new ByteArrayResource(bytes) {
                @Override public String getFilename() { return goodFilename; }
            };
            HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(filePart, fileHeaders);
            body.add("file", fileEntity);

            ResponseEntity<String> uploadResp = restTemplate.postForEntity(
                    BASE_URL + "/files",
                    new HttpEntity<>(body, upHeaders),
                    String.class
            );
            if (!uploadResp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Upload do documento falhou: " + uploadResp.getStatusCode() + " - " + uploadResp.getBody());
            }
            String fileId = mapper.readTree(uploadResp.getBody()).path("id").asText();
            if (fileId == null || fileId.isBlank()) {
                throw new RuntimeException("Upload OK, mas sem file id: " + uploadResp.getBody());
            }

            // 2) Chamada ao /responses pedindo resumo
            String payload = """
        {
          "model": "%s",
          "input": [
            {
              "role": "user",
              "content": [
                { "type": "input_text", "text": "Resuma o documento em 5-7 linhas. Se houver seções, cite os títulos e os principais pontos." },
                { "type": "input_file", "file_id": "%s" }
              ]
            }
          ]
        }
        """.formatted(MODEL_VISION, fileId);

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setBearerAuth(openaiApiKey);
            respHeaders.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> resp = restTemplate.postForEntity(
                    BASE_URL + "/responses",
                    new HttpEntity<>(payload, respHeaders),
                    String.class
            );
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Responses falhou: " + resp.getStatusCode() + " - " + resp.getBody());
            }

            // 3) Extrai o texto de saída
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
            return (sb.length() > 0) ? sb.toString() : resp.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao resumir documento: " + e.getMessage(), e);
        }
    }



    private File base64ToFile(EventEvolution eventEvolution, String typeMessage) {
        String base64 = eventEvolution.getData().getMessage().getBase64();
        String mime = switch (typeMessage) {
            case "audioMessage" -> eventEvolution.getData().getMessage().getAudioMessage().getMimetype();
            case "imageMessage" -> eventEvolution.getData().getMessage().getImageMessage().getMimetype();

            default -> null;
        };

        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(base64);
            String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS").format(LocalDateTime.now(TZ));

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
}
