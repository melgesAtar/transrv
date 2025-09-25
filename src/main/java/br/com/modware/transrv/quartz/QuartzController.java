package br.com.modware.transrv.quartz;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.quartz.*;

@RestController
@RequestMapping("/quartz")
public class QuartzController {

    private final Scheduler scheduler;

    public QuartzController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @GetMapping("/pending")
    public Map<String, Object> getPendingJobs() throws SchedulerException {
        List<Map<String, Object>> triggers = new ArrayList<>();
        
        for(JobKey jobKey : scheduler.getJobKeys(org.quartz.impl.matchers.GroupMatcher.anyGroup())) {
            List<? extends Trigger> jobTriggers = scheduler.getTriggersOfJob(jobKey);
            for(Trigger trigger : jobTriggers) {
                TriggerKey tk = trigger.getKey();
                Trigger.TriggerState state = scheduler.getTriggerState(tk);

                Map<String, Object> triggerMap = new HashMap<>();
                triggerMap.put("jobKey", jobKey.getName());
                triggerMap.put("jobGroup", jobKey.getGroup());
                triggerMap.put("triggerKey", tk.getName());
                triggerMap.put("triggerGroup", tk.getGroup());
                triggerMap.put("triggerState", state.name());
                triggerMap.put("nextFireTime", trigger.getNextFireTime());
                triggerMap.put("prevFireTime", trigger.getPreviousFireTime());
                triggers.add(triggerMap);
            }
        }
            List<Map<String, Object>> running = new ArrayList<>();
            for(JobExecutionContext context : scheduler.getCurrentlyExecutingJobs()) {
                Map<String, Object> runningMap = new HashMap<>();
                JobDetail jobDetail = context.getJobDetail();
                runningMap.put("jobKey", jobDetail.getKey().getName());
                runningMap.put("jobGroup", jobDetail.getKey().getGroup());
                runningMap.put("fireTime", context.getFireTime());
                runningMap.put("scheduleFireTime", context.getScheduledFireTime());
                running.add(runningMap);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("triggers", triggers);
            result.put("running", running);
            return result;
        
    }
}
