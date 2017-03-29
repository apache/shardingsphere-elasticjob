package com.dangdang.ddframe.job.lite.console.domain;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;

@Getter
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class EventTraceDataSourceConfigurations {
    
    private Set<EventTraceDataSourceConfiguration> eventTraceDataSourceConfiguration = new LinkedHashSet<>();
}
