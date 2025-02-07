/*
 * Copyright (c) 2024 Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: Eclipse Dirigible contributors SPDX-License-Identifier: EPL-2.0
 */
package com.codbex.airflow.cfg;

import org.eclipse.dirigible.commons.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public enum AppConfig {

    AIRFLOW_URL("PHOEBE_AIRFLOW_URL", "http://localhost:8080"), //
    AIRFLOW_WORK_DIR("PHOEBE_AIRFLOW_WORK_DIR", "/opt/airflow");

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    private final String key;

    private final String defaultValue;

    AppConfig(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the from base 64 value.
     *
     * @return the from base 64 value
     */
    public String getFromBase64Value() {
        String val = getStringValue();
        return fromBase64(val);
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
     * From base 64.
     *
     * @param string the string
     * @return the string
     */
    private static String fromBase64(String string) {
        return new String(Base64.getDecoder()
                                .decode(string),
                StandardCharsets.UTF_8);
    }

    /**
     * Set value for this config
     *
     * @param value the value to set
     */
    public void setValues(String value) {
        Configuration.set(key, value);
    }

    /**
     * To base 64.
     *
     * @param string the string
     * @return the string
     */
    private static String toBase64(String string) {
        return Base64.getEncoder()
                     .encodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the boolean value.
     *
     * @return the boolean value
     */
    public boolean getBooleanValue() {
        String configValue = getStringValue();
        return Boolean.valueOf(configValue);
    }

    /**
     * Gets the int value.
     *
     * @return the int value
     */
    public int getIntValue() {
        String stringValue = getStringValue();
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException ex) {
            LOGGER.warn("Configuration with key [{}] has invalid non integer value: {}. Returning the defalt value [{}]", key, stringValue,
                    defaultValue, ex);
        }
        return Integer.parseInt(defaultValue);
    }

}
