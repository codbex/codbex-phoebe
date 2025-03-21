/*
 * Copyright (c) 2025 codbex or an codbex affiliate company and contributors
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2025 codbex or an codbex affiliate company and contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package com.codbex.phoebe.ui.tests;

import com.codbex.phoebe.cfg.AppConfig;
import org.eclipse.dirigible.tests.restassured.RestAssuredExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class ProxyIT extends PhoebeIntegrationTest {

    private static final String AIRFLOW_PROXY_PATH = "/services/airflow";

    static {
        AppConfig.AIRFLOW_URL.setValues("https://httpbin.org");
    }

    @Autowired
    private RestAssuredExecutor restAssuredExecutor;

    @Test
    void textProxyPath() {
        // expected to open https://httpbin.org/get
        restAssuredExecutor.execute(() -> given().when()
                                                 .get(AIRFLOW_PROXY_PATH + "/get")
                                                 .then()
                                                 .statusCode(200)
                                                 .body("headers.Host", equalTo("httpbin.org")));
    }
}
