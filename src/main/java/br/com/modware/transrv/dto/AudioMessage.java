package br.com.modware.transrv.dto;

import lombok.Data;

@Data
public class AudioMessage {
    String url;
    String mimetype;
    String fileSha256;
    String fileLength;
    String seconds;
    String ptt;
    String mediaKey;
    String fileEncSha256;
    String directPath;
    String mediaKeyTimestamp;
    String waveform;

}
