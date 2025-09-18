package br.com.modware.transrv.web;

import br.com.modware.transrv.service.DashboardService;
import br.com.modware.transrv.service.TicketService;
import br.com.modware.transrv.service.DashboardBroadcaster;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final TicketService ticketService;
    private final DashboardBroadcaster broadcaster;

    public DashboardController(DashboardService dashboardService, TicketService ticketService, DashboardBroadcaster broadcaster) {
        this.dashboardService = dashboardService;
        this.ticketService = ticketService;
        this.broadcaster = broadcaster;
    }

    @GetMapping("/summary")
    public br.com.modware.transrv.dto.dashboard.DashboardSummaryDTO getSummary() {
        return dashboardService.getSummary();
    }

    // SSE stream for real-time updates
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() { return broadcaster.stream(); }

    // Method to publish updates
    public void publishUpdate() { broadcaster.publishUpdate(); }

    @PostMapping("/tickets/{id}/close")
    public org.springframework.http.ResponseEntity<Void> closeTicket(@PathVariable Long id) {
        ticketService.closeByDashboard(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @PostMapping("/tickets/{id}/incorrect")
    public org.springframework.http.ResponseEntity<Void> markIncorrect(@PathVariable Long id) {
        ticketService.closeAsIncorrect(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}


