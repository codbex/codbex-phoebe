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
package com.codbex.phoebe.ui.tests;

import com.codbex.phoebe.cfg.AppConfig;
import org.eclipse.dirigible.tests.IDE;
import org.eclipse.dirigible.tests.framework.HtmlElementType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AirflowPerspectiveIT extends UserInterfaceIntegrationTest {

    static {
        AppConfig.AIRFLOW_URL.setValues("http://httpbin.org");
    }

    @Autowired
    private IDE ide;

    @Test
    void testPerspective() {
        // expected to open https://httpbin.org
        ide.openPath("/services/web/ide-airflow/index.html");

        browser.assertElementExistsByTypeAndContainsText(HtmlElementType.HEADER2, "httpbin.org");
        //        browser.assertElementExistsByTypeAndText(HtmlElementType.ANCHOR, "Send email to the developer");
    }
}
