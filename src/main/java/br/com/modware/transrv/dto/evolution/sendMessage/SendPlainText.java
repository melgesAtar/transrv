package br.com.modware.transrv.dto.evolution.sendMessage;

import lombok.Data;

import java.util.List;

@Data
public class SendPlainText {
    private String number;
    private String text;
    private Integer delay;
    private boolean linkPreview;
    private boolean mentionsEveryone;
    private List<String> mentioned;

    private Quoted quoted;
}
