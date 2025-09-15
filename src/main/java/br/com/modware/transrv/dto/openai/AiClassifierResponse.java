package br.com.modware.transrv.dto.openai;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AiClassifierResponse {
    @SerializedName("should_open")
    private boolean shouldOpen;
    @SerializedName("employee")
    private String employee;
    @SerializedName("alert_term")
    private String alertTerm;

    public String toString() {
        return "AiClassifierResponse{" +
                "shouldOpen=" + shouldOpen +
                ", alertTerm='" + alertTerm + '\'' +
                '}';
    }
}
