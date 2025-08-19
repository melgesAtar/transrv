package br.com.modware.transrv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
public class EventEvolution {
    private String event;
    private String instance;
    private Data data;
    @JsonProperty("server_url")
    private String serverURL;
    @JsonProperty("date_time")
    private String dateTime;
    private String sender;
    private String apiKey;

}
