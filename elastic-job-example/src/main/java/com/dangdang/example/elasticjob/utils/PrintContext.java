package com.dangdang.example.elasticjob.utils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public final class PrintContext {
    
    private static final String PROCESS_JOB_MESSAGE = "------ %s process: %s at %s ------";
    
    private static final String FETCH_DATA_MESSAGE = "------ %s load sharding items: %s at %s ------";
    
    private static final String PROCESS_DATA_MESSAGE = "------ %s process data: %s at %s ------";
    
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private final Class<?> clazz;
    
    public PrintContext(final Class<?> clazz) {
        this.clazz = clazz;
    }
    
    public void printProcessJobMessage(final Collection<Integer> shardingItems) {
        System.out.println(String.format(PROCESS_JOB_MESSAGE, clazz.getSimpleName(), shardingItems, new SimpleDateFormat(DATE_FORMAT).format(new Date())));
    }
    
    public void printFetchDataMessage(final int shardingItem) {
        System.out.println(String.format(FETCH_DATA_MESSAGE, clazz.getSimpleName(), shardingItem, new SimpleDateFormat(DATE_FORMAT).format(new Date())));
    }
    
    public void printFetchDataMessage(final Collection<Integer> shardingItems) {
        System.out.println(String.format(FETCH_DATA_MESSAGE, clazz.getSimpleName(), shardingItems, new SimpleDateFormat(DATE_FORMAT).format(new Date())));
    }
    
    public void printProcessDataMessage(final Object data) {
        System.out.println(String.format(PROCESS_DATA_MESSAGE, clazz.getSimpleName(), data, new SimpleDateFormat(DATE_FORMAT).format(new Date())));
    }
}
