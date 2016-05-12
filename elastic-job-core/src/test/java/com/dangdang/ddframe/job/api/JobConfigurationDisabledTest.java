package com.dangdang.ddframe.job.api;

import com.dangdang.ddframe.job.internal.env.LocalHostService;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Leon Guo
 */
public class JobConfigurationDisabledTest {

    private final JobConfiguration jobConfiguration = new JobConfiguration("unitTest", ElasticJob.class, 1, "");

    private final LocalHostService localHostService = new LocalHostService();

    @Test
    public void testDisabled0() {
        jobConfiguration.setDisabled(true);
        jobConfiguration.setAllow(localHostService.getIp());
        jobConfiguration.init();
        assertTrue(jobConfiguration.isDisabled());
    }

    @Test
    public void testDisabled1() {
        jobConfiguration.setAllow(localHostService.getIp());
        jobConfiguration.init();
        assertFalse(jobConfiguration.isDisabled());
    }

    @Test
    public void testDisabled2() {
        jobConfiguration.setDeny(localHostService.getIp());
        jobConfiguration.init();
        assertTrue(jobConfiguration.isDisabled());
    }

    @Test
    public void testDisabled3() {
        jobConfiguration.setAllow(localHostService.getIp());
        jobConfiguration.setDeny(localHostService.getIp());
        jobConfiguration.init();
        assertTrue(jobConfiguration.isDisabled());
    }

    @Test
    public void testDisabled4() {
        jobConfiguration.init();
        assertFalse(jobConfiguration.isDisabled());
    }

    @Test
    public void testDisabled5() {
        jobConfiguration.setDisabled(true);
        jobConfiguration.init();
        assertTrue(jobConfiguration.isDisabled());
    }

}
