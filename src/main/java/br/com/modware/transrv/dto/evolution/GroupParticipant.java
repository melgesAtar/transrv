package br.com.modware.transrv.dto.evolution;

import lombok.Data;

@Data
public class GroupParticipant {
    private String id;
    private String jid;
    private String lid;
    private String admin; // "superadmin" ou null
}


