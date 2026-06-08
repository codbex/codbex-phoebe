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

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * A minimal, self-contained HTTP server backed by the JDK (no external dependency) used as the
 * upstream "Airflow" target in proxy integration tests. It serves a fixed HTML body for every
 * request on an OS-assigned ephemeral port, which makes the proxy tests hermetic instead of relying
 * on flaky external hosts (e.g. httpbin.org).
 */
final class LocalHttpStub {

    private final HttpServer server;
    private final String baseUrl;

    private LocalHttpStub(HttpServer server) {
        this.server = server;
        this.baseUrl = "http://localhost:" + server.getAddress()
                                                   .getPort();
    }

    /**
     * Starts a stub server that replies to every request with the given HTML body.
     *
     * @param htmlBody the response body served as {@code text/html}
     * @return the started stub; the caller is responsible for {@link #stop()}
     */
    static LocalHttpStub startServingHtml(String htmlBody) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            byte[] body = htmlBody.getBytes(StandardCharsets.UTF_8);
            server.createContext("/", exchange -> {
                exchange.getResponseHeaders()
                        .set("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream responseBody = exchange.getResponseBody()) {
                    responseBody.write(body);
                }
            });
            server.start();
            return new LocalHttpStub(server);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start local HTTP stub server", ex);
        }
    }

    /**
     * @return the base URL (scheme, host and port) the stub is listening on
     */
    String baseUrl() {
        return baseUrl;
    }

    void stop() {
        server.stop(0);
    }
}
