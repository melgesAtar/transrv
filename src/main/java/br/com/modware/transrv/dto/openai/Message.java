package br.com.modware.transrv.dto.openai;

import lombok.Data;

@Data

public class Message {
    String role;
    String content;
    String refusal;
}
