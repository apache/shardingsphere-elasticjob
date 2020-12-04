package org.apache.shardingsphere.elasticjob.cloud.console.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class AuthenticationInfo {

    private String username;

    private String password;

}
