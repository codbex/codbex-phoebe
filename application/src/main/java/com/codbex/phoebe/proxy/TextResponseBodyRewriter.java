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
package com.codbex.phoebe.proxy;

import com.codbex.phoebe.cfg.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.filter.BodyFilterFunctions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Component
class TextResponseBodyRewriter implements BodyFilterFunctions.RewriteResponseFunction<byte[], byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextResponseBodyRewriter.class);

    private static final String NEW_BASE_PATH = AirflowProxyConfig.ABSOLUTE_BASE_PATH + "/";

    private static final String EXCEPT_STARTS_WITH_BASE_PATH = "(?!" + AirflowProxyConfig.RELATIVE_BASE_PATH + "/)";

    private static final Pattern REPLACE_PATTERN =
            Pattern.compile("(?<=\\b(?:href|src|action|content)=[\"']|fetch\\([\"'])/" + EXCEPT_STARTS_WITH_BASE_PATH);

    private final String airflowUrl;
    private final Pattern fullURLPattern;

    TextResponseBodyRewriter() {
        this.airflowUrl = AppConfig.AIRFLOW_URL.getStringValue();
        this.fullURLPattern = Pattern.compile("(" + airflowUrl + ")(/services/airflow)?(?:/([^\"'\\s<>]*))?");
    }

    @Override
    public byte[] apply(ServerRequest request, ServerResponse response, byte[] originalBody) {
        MediaType contentType = response.headers()
                                        .getContentType();

        if (isTextResponse(contentType)) {
            return rewriteBody(request, response, originalBody, contentType);
        } else {
            LOGGER.debug("Response body will not be modified since content type is [{}]", contentType);
            return originalBody;
        }
    }

    private byte[] rewriteBody(ServerRequest request, ServerResponse response, byte[] originalBody, MediaType contentType) {
        byte[] body = getBody(request, response, originalBody);

        LOGGER.debug("Modifying response body since content type is [{}]", contentType);
        String modifiedBody = rewriteBody(request, body);

        byte[] responseBytes = modifiedBody.getBytes(StandardCharsets.UTF_8);
        response.headers()
                .set(HttpHeaders.CONTENT_LENGTH, String.valueOf(responseBytes.length));

        return responseBytes;
    }

    String rewriteBody(ServerRequest request, byte[] body) {
        String stringBody = new String(body, StandardCharsets.UTF_8);

        String modifiedBody = rewriteRelativePaths(stringBody);
        return rewriteFullURLs(request, modifiedBody);
    }

    private static String rewriteRelativePaths(String stringBody) {
        Matcher matcher = REPLACE_PATTERN.matcher(stringBody);
        return matcher.replaceAll(NEW_BASE_PATH);
    }

    private String rewriteFullURLs(ServerRequest request, String body) {
        URI requestURI = request.uri();
        String requestScheme = requestURI.getScheme();
        String requestHost = requestURI.getHost();
        int port = requestURI.getPort();

        String portString = (port == -1) ? "" : ":" + port;
        String newAirflowUrl = requestScheme + "://" + requestHost + portString + AirflowProxyConfig.ABSOLUTE_BASE_PATH;

        Matcher matcher = fullURLPattern.matcher(body);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String extraPath = matcher.group(3); // Remaining path

            String replacement = newAirflowUrl + (extraPath != null ? "/" + extraPath : "");

            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private byte[] getBody(ServerRequest request, ServerResponse response, byte[] originalBody) {
        if (!isGzipped(response)) {
            return originalBody;
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(originalBody);
                GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {

            response.headers()
                    .remove(HttpHeaders.CONTENT_ENCODING);
            return gzipInputStream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decompress GZIP response from request to " + request.uri(), e);
        }
    }

    private boolean isGzipped(ServerResponse response) {
        String contentEncoding = response.headers()
                                         .getFirst(HttpHeaders.CONTENT_ENCODING);
        return contentEncoding != null && contentEncoding.toLowerCase()
                                                         .contains("gzip");
    }

    private boolean isTextResponse(MediaType contentType) {
        if (null == contentType) {
            return false;
        }

        return contentType.isCompatibleWith(MediaType.TEXT_HTML)//
                || contentType.isCompatibleWith(MediaType.APPLICATION_JSON)//
                || contentType.isCompatibleWith(MediaType.TEXT_PLAIN) //
                || contentType.isCompatibleWith(MediaType.TEXT_XML)//
                || contentType.isCompatibleWith(MediaType.APPLICATION_XML)//
                // || contentType.isCompatibleWith(MimeType.valueOf("text/css"))
                || contentType.isCompatibleWith(MimeType.valueOf("text/javascript"));
    }

}
