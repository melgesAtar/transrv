package br.com.modware.transrv.dto.evolution;

public class ImageMessage {
    private String url;
    private String mimeType;
    private String caption;
    private String fileSha256;
    private String fileLength;
    private Integer height;
    private Integer width;
    private String mediaKey;
    private String fileEncSha256;
    private String directPath;
    private String mediaKeyTimestamp;
    private String jpegThumbnail;


    public String getMimetype() {
        return mimeType;
    }

    public String getCaption() {
        return caption;
    }
}
