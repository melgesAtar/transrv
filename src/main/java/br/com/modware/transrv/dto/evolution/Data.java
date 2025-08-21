package br.com.modware.transrv.dto.evolution;

@lombok.Data
public class Data {
    private Key key;
    private String pushName;
    private String status;
    private Message message;
    private String messageType;
    private String messageTimeStamp;
    private String instanceId;
    private String source;
}
