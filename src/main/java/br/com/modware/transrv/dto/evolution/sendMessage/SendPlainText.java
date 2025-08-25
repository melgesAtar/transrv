package br.com.modware.transrv.dto.evolution.sendMessage;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SendPlainText {
    private String number;
    private String text;
    private Long delay = 0L;
    private boolean linkPreview;
    private boolean mentionsEveryone;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MentionedUser> mentioned = new ArrayList<>();
    private Quoted quoted;

    @Data
    public static class MentionedUser {
        private String id;
    }
}
