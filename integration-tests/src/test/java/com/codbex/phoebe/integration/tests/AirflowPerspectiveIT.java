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
import org.eclipse.dirigible.tests.framework.browser.HtmlElementType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class AirflowPerspectiveIT extends PhoebeIntegrationTest {

    private static final String AIRFLOW_TITLE = "Apache Airflow";

    // A local stub stands in for the Airflow instance so the test does not depend on an external
    // host. The URL must be configured before the Spring context (and the proxy beans) start, hence
    // the static initializer.
    private static final LocalHttpStub AIRFLOW_STUB =
            LocalHttpStub.startServingHtml("<!DOCTYPE html><html><head><title>" + AIRFLOW_TITLE + "</title></head>" //
                    + "<body><h2>" + AIRFLOW_TITLE + "</h2></body></html>");

    static {
        AppConfig.AIRFLOW_URL.setValue(AIRFLOW_STUB.baseUrl());
    }

    @AfterAll
    static void stopStub() {
        AIRFLOW_STUB.stop();
    }

    @Test
    void testPerspective() {
        // the perspective iframes the proxied Airflow UI served by the local stub
        ide.openPath("/services/web/perspective-airflow/index.html");

        browser.assertElementExistsByTypeAndContainsText(HtmlElementType.HEADER2, AIRFLOW_TITLE);
    }
}
