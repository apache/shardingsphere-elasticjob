package org.apache.shardingsphere.elasticjob.lite.console.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * User authentication service.
 **/
@Component
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class UserAuthenticationService {

    private String rootUsername;

    private String rootPassword;

    private String guestUsername;

    private String guestPassword;

    /**
     * Check user.
     *
     * @param authorization authorization
     * @return authorization result
     */
    public AuthenticationResult checkUser(final String authorization) {
        if ((rootUsername + ":" + rootPassword).equals(authorization)) {
            return new AuthenticationResult(true, false);
        } else if ((guestUsername + ":" + guestPassword).equals(authorization)) {
            return new AuthenticationResult(true, true);
        } else {
            return new AuthenticationResult(false, false);
        }
    }
}
