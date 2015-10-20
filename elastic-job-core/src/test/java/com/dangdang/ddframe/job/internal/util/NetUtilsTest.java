package com.dangdang.ddframe.job.internal.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huangtao on 2015/10/19.
 */
public class NetUtilsTest {

    @Test
    public void testGetLocalHost() throws Exception {
        System.out.println(NetUtils.getLocalHost());
    }

    @Test
    public void testGetHostName() throws Exception {
        System.out.println(NetUtils.getHostName(NetUtils.getLocalHost()));
    }




}