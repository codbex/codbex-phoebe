package com.codbex.airflow.proxy;

import com.codbex.airflow.cfg.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
class RelativeLocationHeaderRewriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelativeLocationHeaderRewriter.class);

    private final String airflowUrl;

    RelativeLocationHeaderRewriter() {
        this.airflowUrl = AppConfig.AIRFLOW_URL.getStringValue();
    }

    ServerResponse rewriteRelativeLocationHeader(ServerRequest request, ServerResponse response) {
        String locationHeader = response.headers()
                                        .getFirst(HttpHeaders.LOCATION);
        if (null == locationHeader) {
            LOGGER.debug("Missing location header. Nothing to rewrite.");
            return response;
        }

        String newLocationHeader = locationHeader;

        // handle cases like location: /login/?next=http://localhost:8080/home
        String encodedTarget = URLEncoder.encode(airflowUrl, StandardCharsets.UTF_8);
        String encodedFullBaseURL = URLEncoder.encode(airflowUrl + AirflowProxyConfig.ABSOLUTE_BASE_PATH, StandardCharsets.UTF_8);
        if (locationHeader.contains(encodedTarget) && !locationHeader.contains(encodedFullBaseURL)) {
            newLocationHeader = locationHeader.replace(encodedTarget, encodedFullBaseURL);
        }

        if (newLocationHeader.startsWith("/")) {
            newLocationHeader = newLocationHeader.replaceFirst("/", (AirflowProxyConfig.ABSOLUTE_BASE_PATH + "/"));
        }

        response.headers()
                .set(HttpHeaders.LOCATION, newLocationHeader);
        LOGGER.debug("Replaced location header [{}] with [{}]", locationHeader, newLocationHeader);

        return response;
    }
}
