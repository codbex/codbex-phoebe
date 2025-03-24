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

import org.eclipse.dirigible.tests.framework.HtmlElementType;
import org.junit.jupiter.api.Test;

class HomePageIT extends PhoebeIntegrationTest {

    @Test
    void testOpenHomepage() {
        ide.openHomePage();

        browser.assertElementExistsByTypeAndText(HtmlElementType.SPAN, "Phoebe");
        browser.assertElementExistsByTypeAndText(HtmlElementType.HEADER3, "Welcome to Phoebe");
    }
}
