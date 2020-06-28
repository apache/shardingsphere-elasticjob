package org.apache.shardingsphere.elasticjob.lite.console.config;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.apache.shardingsphere.elasticjob.lite.console.filter.GlobalConfigurationFilter;
import org.apache.shardingsphere.elasticjob.lite.console.security.UserAuthenticationService;
import org.apache.shardingsphere.elasticjob.lite.console.security.WwwAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter register config.
 **/
@Configuration
public class FilterRegisterConfig {

    private UserAuthenticationService userAuthenticationService;

    @Autowired
    public FilterRegisterConfig(final UserAuthenticationService userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }

    /**
     * register global configuration filter.
     *
     * @return global configuration filter bean
     */
    @Bean
    public FilterRegistrationBean<GlobalConfigurationFilter> globalConfigurationFilter() {
        GlobalConfigurationFilter globalConfigurationFilter = new GlobalConfigurationFilter();
        FilterRegistrationBean<GlobalConfigurationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(globalConfigurationFilter);
        registration.addUrlPatterns("*.html");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        return registration;
    }

    /**
     * register www auth filter.
     *
     * @return www auth filter bean
     */
    @Bean
    public FilterRegistrationBean<WwwAuthFilter> wwwAuthFilter() {
        WwwAuthFilter wwwAuthFilter = new WwwAuthFilter();
        wwwAuthFilter.setUserAuthenticationService(userAuthenticationService);
        FilterRegistrationBean<WwwAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(wwwAuthFilter);
        registration.addUrlPatterns("/");
        registration.addUrlPatterns("*.html");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        return registration;
    }
}
