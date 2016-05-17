package com.dangdang.ddframe.job.internal.network;

import com.dangdang.ddframe.job.internal.env.LocalHostService;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Leon Guo
 */
public class LocalIPFilterTest {

    private final LocalHostService localHostService = new LocalHostService();

    private  LocalIPFilter ipFilter;

    @Test
    public void testAllow() {
        String localIp = localHostService.getIp();
        ipFilter = new LocalIPFilter(localIp, null);
        assertTrue(ipFilter.isAllowed());
        assertFalse(ipFilter.isDenied());
    }

    @Test
    public void testDeny() {
        String localIp = localHostService.getIp();
        ipFilter = new LocalIPFilter(null, localIp);
        assertTrue(ipFilter.isDenied());
        assertTrue(ipFilter.isAllowed());
    }

    @Test
    public void testDenyAllow() {
        String localIp = localHostService.getIp();
        ipFilter = new LocalIPFilter(localIp, localIp);
        assertTrue(ipFilter.isDenied());
        assertTrue(ipFilter.isAllowed());
    }

}
