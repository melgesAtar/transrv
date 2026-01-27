package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.dashboard.DashboardSummaryDTO;
import br.com.modware.transrv.dto.dashboard.AlertNotificationDTO;
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
        DashboardSummaryDTO data;
        try {
            data = dashboardService.getSummary();
        } catch (Exception ex) {
            // Falha ao montar o resumo não deve quebrar o fluxo do backend
            return;
        }

        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("update").data(data));
            } catch (IOException ex) {
                try {
                    e.complete();
                } catch (Exception ignored) {}
                emitters.remove(e);
            } catch (IllegalStateException ex) {
                // Pode ocorrer se a conexão SSE já foi encerrada pelo container
                try {
                    e.complete();
                } catch (Exception ignored) {}
                emitters.remove(e);
            } catch (Exception ex) {
                // Qualquer outra exceção não deve interromper o processamento do request atual
                try {
                    e.complete();
                } catch (Exception ignored) {}
                emitters.remove(e);
            }
        }
    }

    public void publishAlert(AlertNotificationDTO alert) {
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("alert").data(alert));
            } catch (IOException ex) {
                try {
                    e.complete();
                } catch (Exception ignored) {}
                emitters.remove(e);
            } catch (IllegalStateException ex) {
                try {
                    e.complete();
                } catch (Exception ignored) {}
                emitters.remove(e);
            } catch (Exception ex) {
                try {
                    e.complete();
                } catch (Exception ignored) {}
                emitters.remove(e);
            }
        }
    }
}


