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
package com.codbex.phoebe.cfg;

import org.eclipse.dirigible.commons.config.Configuration;

public enum AppConfig {

    AIRFLOW_URL("PHOEBE_AIRFLOW_URL", "http://localhost:8080"), //
    AIRFLOW_WORK_DIR("PHOEBE_AIRFLOW_WORK_DIR", "/opt/airflow");

    private final String key;

    private final String defaultValue;

    AppConfig(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    /**
     * Gets the string value.
     *
     * @return the string value
     */
    public String getStringValue() {
        return Configuration.get(key, defaultValue);
    }

    /**
     * Set value for this config
     *
     * @param value the value to set
     */
    public void setValue(String value) {
        Configuration.set(key, value);
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

}
