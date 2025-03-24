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

import org.eclipse.dirigible.tests.WelcomeView;
import org.eclipse.dirigible.tests.Workbench;
import org.eclipse.dirigible.tests.framework.HtmlAttribute;
import org.eclipse.dirigible.tests.framework.HtmlElementType;
import org.junit.jupiter.api.Test;

class AiflowStarterTemplateIT extends PhoebeIntegrationTest {

    private static final String TEMPLATE_TITLE = "Apache Airflow Starter";
    private static final String TEST_PROJECT = AiflowStarterTemplateIT.class.getSimpleName();

    @Test
    void testCreateFromTemplate() {
        ide.openHomePage();
        Workbench workbench = ide.openWorkbench();

        WelcomeView welcomeView = workbench.openWelcomeView();
        welcomeView.searchForTemplate(TEMPLATE_TITLE);
        welcomeView.selectTemplate(TEMPLATE_TITLE);

        welcomeView.typeProjectName(TEST_PROJECT);
        welcomeView.typeFileName(TEST_PROJECT);
        browser.enterTextInElementByAttributePattern(HtmlElementType.INPUT, HtmlAttribute.ID, "param_dagId", TEST_PROJECT);
        welcomeView.confirmTemplate();

        workbench.publishAll(true);
    }
}
