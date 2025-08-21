package br.com.modware.transrv.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AiClassifierResponse {
    @SerializedName("should_open")
    boolean shouldOpen;
    @SerializedName("alert_term")
    String alertTerm;

    public String toString() {
        return "AiClassifierResponse{" +
                "shouldOpen=" + shouldOpen +
                ", alertTerm='" + alertTerm + '\'' +
                '}';
    }
}
