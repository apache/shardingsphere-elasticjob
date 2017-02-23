package com.dangdang.ddframe.job.lite.console;

import java.util.Date;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;

public class SimpleDemoJob implements SimpleJob {
        @Override
        public void execute(ShardingContext context) {
            switch (context.getShardingItem()) {
                case 0: 
                    System.out.println(new Date() + "：现在打印0");
                    break;
                case 1: 
                    System.out.println(new Date() + "：现在打印1");
                    break;
                case 2: 
                    System.out.println(new Date() + "：现在打印2");
                    break;
            }
        }
    }
