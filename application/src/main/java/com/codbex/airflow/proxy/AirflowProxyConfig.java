package com.codbex.airflow.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.AfterFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.regex.Pattern;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class AirflowProxyConfig {

    static final String RELATIVE_BASE_PATH = "services/airflow";
    static final String ABSOLUTE_BASE_PATH = "/" + RELATIVE_BASE_PATH;
    static final String BASE_PATH_PATTERN = ABSOLUTE_BASE_PATH + "/**";

    private static final String EXCEPT_STARTS_WITH_BASE_PATH = "(?!" + RELATIVE_BASE_PATH + "/)";
    private static final Pattern REPLACE_PATTERN =
            Pattern.compile("(?<=\\b(?:href|src|action|content|fetch\\(\")=['\"]?)/" + EXCEPT_STARTS_WITH_BASE_PATH);

    private final String airflowUrl;
    private final RelativeLocationHeaderRewriter relativeLocationHeaderRewriter;
    private final TextResponseBodyRewriter textResponseBodyRewriter;

    AirflowProxyConfig(@Value("${airflow.url}") String airflowUrl, RelativeLocationHeaderRewriter relativeLocationHeaderRewriter,
            TextResponseBodyRewriter textResponseBodyRewriter) {
        this.airflowUrl = airflowUrl;
        this.relativeLocationHeaderRewriter = relativeLocationHeaderRewriter;
        this.textResponseBodyRewriter = textResponseBodyRewriter;
    }

    @Bean
    RouterFunction<ServerResponse> configureAirflowProxy() {
        return GatewayRouterFunctions.route("airflow-proxy-route")
                                     .GET(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .POST(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .PUT(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .PATCH(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .DELETE(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .HEAD(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))
                                     .OPTIONS(path(AirflowProxyConfig.BASE_PATH_PATTERN), http(airflowUrl))

                                     .before(BeforeFilterFunctions.rewritePath(AirflowProxyConfig.ABSOLUTE_BASE_PATH + "(.*)", "$1"))
                                     .before(BeforeFilterFunctions.addRequestHeader(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46YWRtaW4="))

                                     .after(AfterFilterFunctions.rewriteLocationResponseHeader(
                                             cfg -> cfg.setProtocolsRegex("https?|ftps?|http?")))
                                     .after(relativeLocationHeaderRewriter::rewriteRelativeLocationHeader)
                                     .after(AfterFilterFunctions.modifyResponseBody(byte[].class, byte[].class, null,
                                             textResponseBodyRewriter))
                                     .build();
    }
}
