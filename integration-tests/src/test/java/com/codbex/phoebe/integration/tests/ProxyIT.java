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
package com.codbex.phoebe.integration.tests;

import com.codbex.phoebe.cfg.AppConfig;
import org.eclipse.dirigible.tests.framework.restassured.RestAssuredExecutor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

class ProxyIT extends PhoebeIntegrationTest {

    private static final String AIRFLOW_PROXY_PATH = "/services/airflow";

    // The stub returns a root-relative link; the proxy is expected to rewrite it so it stays within
    // the "/services/airflow" prefix when the UI is served behind the proxy.
    private static final LocalHttpStub AIRFLOW_STUB =
            LocalHttpStub.startServingHtml("<html><body><a href=\"/airflow/home\">home</a></body></html>");

    static {
        AppConfig.AIRFLOW_URL.setValue(AIRFLOW_STUB.baseUrl());
    }

    @Autowired
    private RestAssuredExecutor restAssuredExecutor;

    @AfterAll
    static void stopStub() {
        AIRFLOW_STUB.stop();
    }

    @Test
    void textProxyPath() {
        // the proxy forwards to the local stub and rewrites the root-relative link in the response
        // body from "/airflow/home" to "/services/airflow/airflow/home"
        restAssuredExecutor.execute(() -> given().when()
                                                 .get(AIRFLOW_PROXY_PATH + "/home")
                                                 .then()
                                                 .statusCode(200)
                                                 .body(containsString("href=\"" + AIRFLOW_PROXY_PATH + "/airflow/home\"")));
    }
}
