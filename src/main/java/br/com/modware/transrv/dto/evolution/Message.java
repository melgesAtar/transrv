package br.com.modware.transrv.dto.evolution;

import lombok.Data;

@Data
public class Message {
    private String conversation;
    private AudioMessage audioMessage;
    private ImageMessage imageMessage;
    private DocumentMessage documentMessage;
    private SenderKeyDistributionMessage senderKeyDistributionMessage;
    private String base64;
}
