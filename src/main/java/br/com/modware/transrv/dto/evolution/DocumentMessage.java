package br.com.modware.transrv.dto.evolution;

import com.google.gson.JsonElement;

public class DocumentMessage {
    private String url;
    private String mimetype;
    private String title;
    private JsonElement fileSha256;
    private String fileLength;
    private String pageCount;
    private JsonElement mediaKey;
    private String fileName;
    private JsonElement fileEncSha256;
    private String directPath;
    // mediaKeyTimestamp pode vir como string ou objeto
    private JsonElement mediaKeyTimestamp;
    private boolean contactVCard;
    private JsonElement thumbnailDirectPath;
    private JsonElement thumbnailSha256;
    private JsonElement thumbnailEncSha256;
    private JsonElement jpegThumbnail;
    private Integer thumbnailHeight;
    private Integer thumbnailWidth;

    public String getMimeType() {
        return mimetype;
    }

    public String getFileName() {
        return fileName;
    }
}
