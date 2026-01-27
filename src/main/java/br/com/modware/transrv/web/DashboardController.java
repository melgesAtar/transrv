package br.com.modware.transrv.web;

import br.com.modware.transrv.service.DashboardService;
import org.springframework.beans.factory.annotation.Value;
import br.com.modware.transrv.dto.dashboard.DashboardConfigDTO;
import br.com.modware.transrv.service.TicketService;
import br.com.modware.transrv.service.DashboardBroadcaster;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.quartz.SchedulerException;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final TicketService ticketService;
    private final DashboardBroadcaster broadcaster;
    @Value("${app.escalation.level2.delay-seconds:300}")
    private long escalationLevel2DelaySeconds;
    @Value("${app.escalation.level3.delay-seconds:600}")
    private long escalationLevel3DelaySeconds;

    public DashboardController(DashboardService dashboardService, TicketService ticketService, DashboardBroadcaster broadcaster) {
        this.dashboardService = dashboardService;
        this.ticketService = ticketService;
        this.broadcaster = broadcaster;
    }

    @GetMapping("/summary")
    public br.com.modware.transrv.dto.dashboard.DashboardSummaryDTO getSummary() {
        return dashboardService.getSummary();
    }

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

    @GetMapping("/config")
    public DashboardConfigDTO getConfig() {
        DashboardConfigDTO dto = new DashboardConfigDTO();
        dto.setEscalationLevel2Seconds(escalationLevel2DelaySeconds);
        dto.setEscalationLevel3Seconds(escalationLevel3DelaySeconds);
        return dto;
    }

    @PostMapping("/test-ticket/open")
    public org.springframework.http.ResponseEntity<Void> openTestTicket() throws SchedulerException {
        ticketService.openTestTicket();
        return org.springframework.http.ResponseEntity.ok().build();
    }
}


