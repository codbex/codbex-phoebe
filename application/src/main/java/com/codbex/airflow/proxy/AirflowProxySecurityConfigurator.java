package com.codbex.airflow.proxy;

import org.eclipse.dirigible.components.base.http.access.CustomSecurityConfigurator;
import org.eclipse.dirigible.components.base.http.roles.Roles;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

@Component
class AirflowProxySecurityConfigurator implements CustomSecurityConfigurator {
    private static final String[] DEVELOPER_PATTERNS = { //
            AirflowProxyConfig.BASE_PATH_PATTERN};

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authz) -> //
                authz.requestMatchers(DEVELOPER_PATTERNS)
                     .hasRole(Roles.DEVELOPER.getRoleName()));
    }
}
