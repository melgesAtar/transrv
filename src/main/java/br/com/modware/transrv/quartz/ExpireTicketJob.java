package br.com.modware.transrv.quartz;

import br.com.modware.transrv.service.TicketService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpireTicketJob implements Job {

    @Autowired
    private TicketService ticketService;

    public ExpireTicketJob() {}

    @Override
    public void execute(JobExecutionContext context) {
        Long ticketId = context.getMergedJobDataMap().getLong("ticketId");
        ticketService.expire(ticketId);
    }
}
