package br.com.modware.transrv.dto.evolution;

import com.google.gson.JsonElement;

public class ImageMessage {
    private String url;
    private String mimeType;
    private String caption;
    // Alguns provedores enviam sha/enc sha/thumbnail como objeto em vez de string
    private JsonElement fileSha256;
    private String fileLength;
    private Integer height;
    private Integer width;
    private String mediaKey;
    private JsonElement fileEncSha256;
    private String directPath;
    private String mediaKeyTimestamp;
    private JsonElement jpegThumbnail;


    public String getMimetype() {
        return mimeType;
    }

    public String getCaption() {
        return caption;
    }
}
