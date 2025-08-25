package br.com.modware.transrv.quartz;

import br.com.modware.transrv.service.TicketService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EscalationJob implements Job {

    @Autowired
    private TicketService ticketService;

    @Override
    public void execute(JobExecutionContext context) {
        Long ticketId = context.getJobDetail().getJobDataMap().getLong("ticketId");
        int level = context.getJobDetail().getJobDataMap().getInt("level");
        ticketService.escalate(ticketId, level);
    }
}
