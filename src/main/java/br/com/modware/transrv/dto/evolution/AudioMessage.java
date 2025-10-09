package br.com.modware.transrv.dto.evolution;

import com.google.gson.JsonElement;
import lombok.Data;

@Data
public class AudioMessage {
    String url;
    String mimetype;
    // Alguns provedores enviam como objeto ao invés de string
    JsonElement fileSha256;
    String fileLength;
    String seconds;
    String ptt;
    JsonElement mediaKey;
    JsonElement fileEncSha256;
    String directPath;
    String mediaKeyTimestamp;
    JsonElement waveform;

}
