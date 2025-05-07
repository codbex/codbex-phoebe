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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

class ProxyIT extends PhoebeIntegrationTest {

    private static final String AIRFLOW_PROXY_PATH = "/services/airflow";

    static {
        AppConfig.AIRFLOW_URL.setValues("https://api.ipify.org");
    }

    @Autowired
    private RestAssuredExecutor restAssuredExecutor;

    @Test
    void textProxyPath() {
        // expected to open https://api.ipify.org?format=json
        restAssuredExecutor.execute(() -> given().when()
                                                 .get(AIRFLOW_PROXY_PATH + "?format=json")
                                                 .then()
                                                 .statusCode(200)
                                                 .body("ip", notNullValue()));
    }
}
