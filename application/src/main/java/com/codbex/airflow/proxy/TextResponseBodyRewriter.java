package com.codbex.airflow.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.BodyFilterFunctions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
            Pattern.compile("(?<=\\b(?:href|src|action|content|fetch\\(\")=['\"]?)/" + EXCEPT_STARTS_WITH_BASE_PATH);

    private final String airflowUrl;

    TextResponseBodyRewriter(@Value("${airflow.url}") String airflowUrl) {
        this.airflowUrl = airflowUrl;
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
        String stringBody = new String(body, StandardCharsets.UTF_8);

        Matcher matcher = REPLACE_PATTERN.matcher(stringBody);
        String modifiedBody = matcher.replaceAll(NEW_BASE_PATH);

        String requestHost = request.headers()
                                    .firstHeader(HttpHeaders.HOST);
        if (requestHost == null || requestHost.isEmpty()) {
            LOGGER.debug("Missing Host header in request. Skipping Airflow URL replacement.");
        } else {
            String requestScheme = request.uri()
                                          .getScheme();
            String newAirflowUrl = requestScheme + "://" + requestHost + AirflowProxyConfig.ABSOLUTE_BASE_PATH;

            modifiedBody = modifiedBody.replace(airflowUrl, newAirflowUrl);
        }

        byte[] responseBytes = modifiedBody.getBytes(StandardCharsets.UTF_8);
        response.headers()
                .set(HttpHeaders.CONTENT_LENGTH, String.valueOf(responseBytes.length));

        return responseBytes;
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
