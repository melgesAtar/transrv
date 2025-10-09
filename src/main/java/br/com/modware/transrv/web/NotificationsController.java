package br.com.modware.transrv.web;

import br.com.modware.transrv.dto.dashboard.NotificationDTO;
import br.com.modware.transrv.service.NotificationsBroadcaster;
import br.com.modware.transrv.service.NotificationsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final NotificationsService service;
    private final NotificationsBroadcaster broadcaster;

    public NotificationsController(NotificationsService service, NotificationsBroadcaster broadcaster) {
        this.service = service;
        this.broadcaster = broadcaster;
    }

    @GetMapping
    public List<NotificationDTO> list() {
        return service.getRecent();
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return broadcaster.stream();
    }
}


