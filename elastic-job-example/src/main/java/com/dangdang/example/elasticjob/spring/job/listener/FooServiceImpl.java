package com.dangdang.example.elasticjob.spring.job.listener;

import org.springframework.stereotype.Component;

@Component
public class FooServiceImpl implements FooService {
    
    public String foo() {
        return "this is foo."; 
    }
}
