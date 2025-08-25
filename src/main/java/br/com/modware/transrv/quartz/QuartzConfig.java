package br.com.modware.transrv.quartz;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;


@Configuration
public class QuartzConfig {

    private final AutowiringSpringBeanJobFactory jobFactory;

    public QuartzConfig(AutowiringSpringBeanJobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {
        factory.setJobFactory(jobFactory);
        return factory.getScheduler();
    }
}
