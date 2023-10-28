package org.apache.shardingsphere.elasticjob.spi.service;

/**
 * Job runtime service.
 */
public interface JobRuntimeService {
    
    /**
     * Judge job whether to need resharding.
     *
     * @return need resharding or not
     */
    boolean isNeedSharding();
}
