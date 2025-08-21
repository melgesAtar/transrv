package br.com.modware.transrv.dto.evolution;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentMessage {
    private String url;
    private String mimetype;
    private String title;
    private String fileSha256;
    private String fileLength;
    private String pageCount;
    private String mediaKey;
    private String fileName;
    private String fileEncSha256;
    private String directPath;
    private String mediaKeyTimestamp;
    private boolean contactVCard;
    private String thumbnailDirectPath;
    private String thumbnailSha256;
    private String thumbnailEncSha256;
    private String jpegThumbnail;
    private Integer thumbnailHeight;
    private Integer thumbnailWidth;

    public String getMimeType() {
        return mimetype;
    }

    public String getFileName() {
        return fileName;
    }
}
