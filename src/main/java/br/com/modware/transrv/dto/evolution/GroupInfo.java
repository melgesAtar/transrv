package br.com.modware.transrv.dto.evolution;

import lombok.Data;

import java.util.List;

@Data
public class GroupInfo {
    private String id;
    private String subject;
    private String subjectOwner;
    private Long subjectTime;
    private String pictureUrl;
    private Integer size;
    private Long creation;
    private String owner;
    private Boolean restrict;
    private Boolean announce;
    private List<GroupParticipant> participants;
    private Boolean isCommunity;
    private Boolean isCommunityAnnounce;
}


