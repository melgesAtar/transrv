package br.com.modware.transrv.dto.evolution;

import com.google.gson.annotations.SerializedName;

@lombok.Data
public class EventEvolution {
    private String event;
    private String instance;
    private Data data;
    @SerializedName("server_url")
    private String serverURL;
    @SerializedName("date_time")
    private String dateTime;
    private String sender;
    private String apiKey;

}
