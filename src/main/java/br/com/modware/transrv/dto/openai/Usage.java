package br.com.modware.transrv.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Usage {
    @SerializedName("prompt_tokens")
    private int promptTokens;
    @SerializedName("completion_tokens")
    private int completionTokens;
    @SerializedName("total_tokens")
    private int totalTokens;
}
