package br.com.modware.transrv.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ResponseClassifierMessage {
    private String id;
    private String object;
    private String created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    @JsonProperty("service_tier")
    private String serviceTier;
    @JsonProperty("system_fingerprint")
    private String systemFingerPrint;

    public String toString() {
        return "ResponseClassifierMessage{" +
                "id='" + id + '\'' +
                ", object='" + object + '\'' +
                ", created='" + created + '\'' +
                ", model='" + model + '\'' +
                ", choices=" + choices +
                ", usage=" + usage +
                ", serviceTier='" + serviceTier + '\'' +
                ", systemFingerPrint='" + systemFingerPrint + '\'' +
                '}';
    }
}
