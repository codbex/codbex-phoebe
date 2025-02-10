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

import com.codbex.phoebe.cfg.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.filter.AfterFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class AirflowProxyConfig {

    // the path is used in file
    // components/ide/ide-ui-airflow/src/main/resources/META-INF/dirigible/ide-airflow/airflow.html
    static final String RELATIVE_BASE_PATH = "services/airflow";
    static final String ABSOLUTE_BASE_PATH = "/" + RELATIVE_BASE_PATH;
    static final String BASE_PATH_PATTERN = ABSOLUTE_BASE_PATH + "/**";

    private static final Logger LOGGER = LoggerFactory.getLogger(AirflowProxyConfig.class);

    private final RelativeLocationHeaderRewriter relativeLocationHeaderRewriter;
    private final TextResponseBodyRewriter textResponseBodyRewriter;

    AirflowProxyConfig(RelativeLocationHeaderRewriter relativeLocationHeaderRewriter, TextResponseBodyRewriter textResponseBodyRewriter) {
        this.relativeLocationHeaderRewriter = relativeLocationHeaderRewriter;
        this.textResponseBodyRewriter = textResponseBodyRewriter;
    }

    @Bean
    RouterFunction<ServerResponse> configureAirflowProxy() {
        String airflowUrl = AppConfig.AIRFLOW_URL.getStringValue();

        LOGGER.info("Configuring Airflow proxy for path [{}] to URL [{}]", BASE_PATH_PATTERN, airflowUrl);

        return GatewayRouterFunctions.route("airflow-proxy-route")
                                     .GET(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .POST(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .PUT(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .PATCH(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .DELETE(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .HEAD(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .OPTIONS(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))

                                     .before(BeforeFilterFunctions.rewritePath(AirflowProxyConfig.ABSOLUTE_BASE_PATH + "(.*)", "$1"))

                                     .after(AfterFilterFunctions.rewriteLocationResponseHeader(
                                             cfg -> cfg.setProtocolsRegex("https?|ftps?|http?")))
                                     .after(relativeLocationHeaderRewriter::rewriteRelativeLocationHeader)
                                     .after(AfterFilterFunctions.modifyResponseBody(byte[].class, byte[].class, null,
                                             textResponseBodyRewriter))
                                     .build();
    }
}
