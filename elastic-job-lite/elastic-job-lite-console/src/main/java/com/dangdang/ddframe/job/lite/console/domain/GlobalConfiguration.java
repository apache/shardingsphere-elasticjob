package com.dangdang.ddframe.job.lite.console.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class GlobalConfiguration {
    
    private RegistryCenterConfigurations registryCenterConfigurations;
    
    private EventTraceDataSourceConfigurations eventTraceDataSourceConfigurations;
}
