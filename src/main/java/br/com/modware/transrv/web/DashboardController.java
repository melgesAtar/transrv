package br.com.modware.transrv.web;

import br.com.modware.transrv.service.DashboardService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public br.com.modware.transrv.dto.dashboard.DashboardSummaryDTO getSummary() {
        return dashboardService.getSummary();
    }

    // SSE stream for real-time updates
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("update").data(dashboardService.getSummary()));
        } catch (IOException ignored) {}
        return emitter;
    }

    // Method to publish updates
    public void publishUpdate() {
        br.com.modware.transrv.dto.dashboard.DashboardSummaryDTO data = dashboardService.getSummary();
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


