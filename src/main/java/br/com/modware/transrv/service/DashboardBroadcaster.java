package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.dashboard.DashboardSummaryDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DashboardBroadcaster {

    private final DashboardService dashboardService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public DashboardBroadcaster(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

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

    public void publishUpdate() {
        DashboardSummaryDTO data = dashboardService.getSummary();
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


