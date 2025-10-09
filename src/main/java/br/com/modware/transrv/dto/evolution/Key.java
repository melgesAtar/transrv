package br.com.modware.transrv.dto.evolution;

import lombok.Data;

@Data
public class Key {
    private String remoteJid;
    private boolean fromMe;
    private String id;
    private String participant;
    private String participantAlt;
}
