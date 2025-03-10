/*
 * Copyright (c) 2022 codbex or an codbex affiliate company and contributors
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 codbex or an codbex affiliate company and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package com.codbex.phoebe.proxy;

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
