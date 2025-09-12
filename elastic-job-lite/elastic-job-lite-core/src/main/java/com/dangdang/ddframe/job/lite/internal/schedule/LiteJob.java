package com.dangdang.ddframe.job.lite.internal.schedule;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.executor.JobExecutorFactory;
import com.dangdang.ddframe.job.executor.JobFacade;
import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Lite调度作业.
 *
 * @author zhangliang
 */
public final class LiteJob implements Job {

    @Setter
    private ElasticJob elasticJob;

    @Setter
    private JobFacade jobFacade;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        Object ej = dataMap.get("elasticJob");
        Object jf = dataMap.get("jobFacade");

        if (!(ej instanceof ElasticJob) || !(jf instanceof JobFacade)) {
            throw new JobExecutionException("Invalid JobDataMap: elasticJob or jobFacade not set correctly.");
        }

        this.elasticJob = (ElasticJob) ej;
        this.jobFacade = (JobFacade) jf;

        try {
            JobExecutorFactory.getJobExecutor(elasticJob, jobFacade).execute();
        } catch (Exception ex) {
            throw new JobExecutionException("LiteJob execution failed", ex);
        }
    }
}
