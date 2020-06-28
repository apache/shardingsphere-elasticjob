package org.apache.shardingsphere.elasticjob.lite.console.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Authentication result.
 **/
@Getter
@Setter
@RequiredArgsConstructor
public class AuthenticationResult {

    private final boolean success;

    private final boolean isGuest;

}
