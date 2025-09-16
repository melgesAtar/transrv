package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.dashboard.NotificationDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationsBroadcaster {

    private final NotificationsService notificationsService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public NotificationsBroadcaster(NotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }

    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("update").data(notificationsService.getRecent()));
        } catch (IOException ignored) {}
        return emitter;
    }

    public void publishUpdate() {
        List<NotificationDTO> data = notificationsService.getRecent();
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("update").data(data));
            } catch (IOException ex) {
                e.complete();
                emitters.remove(e);
            }
        }
    }
}


