package org.apache.shardingsphere.elasticjob.lite.console.dao.search;

import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.lite.tracing.rdb.storage.RDBJobEventStorage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;

@TestConfiguration
public class RDBJobEventSearchTestConfiguration implements InitializingBean {
    
    @Autowired
    private RDBJobEventSearch repository;
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        initStorage();
    }
    
    private void initStorage() throws SQLException {
        repository.findJobExecutionEvents(new RDBJobEventSearch.Condition(10, 1, null, null, null, null, null));
        repository.findJobStatusTraceEvents(new RDBJobEventSearch.Condition(10, 1, null, null, null, null, null));
        RDBJobEventStorage storage = new RDBJobEventStorage(dataSource);
        for (int i = 1; i <= 500L; i++) {
            JobExecutionEvent startEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job_" + i, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
            storage.addJobExecutionEvent(startEvent);
            if (i % 2 == 0) {
                JobExecutionEvent successEvent = startEvent.executionSuccess();
                storage.addJobExecutionEvent(successEvent);
            }
            storage.addJobStatusTraceEvent(new JobStatusTraceEvent(
                    "test_job_" + i,
                    "fake_failed_failover_task_id",
                    "fake_slave_id",
                    JobStatusTraceEvent.Source.LITE_EXECUTOR,
                    "FAILOVER",
                    "0",
                    JobStatusTraceEvent.State.TASK_FAILED,
                    "message is empty."
            ));
        }
    }
    
}
