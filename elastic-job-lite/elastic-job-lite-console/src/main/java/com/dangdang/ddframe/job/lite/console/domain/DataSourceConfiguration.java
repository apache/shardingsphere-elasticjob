package com.dangdang.ddframe.job.lite.console.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"driver", "url", "username", "password", "activated"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class DataSourceConfiguration implements Serializable {
    
    private static final long serialVersionUID = -5996257770767863699L;
    
    @XmlAttribute(required = true)
    private String name;
    
    @XmlAttribute(required = true)
    private String driver;
    
    @XmlAttribute
    private String url;
    
    @XmlAttribute
    private String username;
    
    @XmlAttribute
    private String password;
    
    @XmlAttribute
    private boolean activated;
    
    public DataSourceConfiguration(final String name, final String driver, final String url, final String username) {
        this.name = name;
        this.driver = driver;
        this.url = url;
        this.username = username;
    }
}
