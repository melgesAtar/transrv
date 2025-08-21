package br.com.modware.transrv.dto.openai;

import lombok.Data;

@Data
public class Choice{
    private Integer index;
    private Message message;
    private String logprobs;
    private String finishReason;
}
