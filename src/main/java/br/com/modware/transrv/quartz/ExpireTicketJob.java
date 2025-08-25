package br.com.modware.transrv.quartz;

import br.com.modware.transrv.service.TicketService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class ExpireTicketJob implements Job {
    private final TicketService ticketService;

    public ExpireTicketJob(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        Long ticketId = context.getMergedJobDataMap().getLong("ticketId");
        ticketService.expire(ticketId);
    }
}
