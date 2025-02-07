package com.codbex.airflow.proxy;

import com.codbex.airflow.cfg.AppConfig;
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
            Pattern.compile("(?<=\\b(?:href|src|action|content)=[\"']|fetch\\(\")/" + EXCEPT_STARTS_WITH_BASE_PATH);
    private final String airflowUrl;

    TextResponseBodyRewriter() {
        this.airflowUrl = AppConfig.AIRFLOW_URL.getStringValue();
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

    public static void main(String[] args) {
        String stringBody = "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n"
                + "    <title>httpbin.org</title>\n"
                + "    <link href=\"https://fonts.googleapis.com/css?family=Open+Sans:400,700|Source+Code+Pro:300,600|Titillium+Web:400,600,700\"\n"
                + "        rel=\"stylesheet\">\n"
                + "    <link rel=\"stylesheet\" type=\"text/css\" href=\"/flasgger_static/swagger-ui.css\">\n"
                + "    <link rel=\"icon\" type=\"image/png\" href=\"/static/favicon.ico\" sizes=\"64x64 32x32 16x16\" />\n"
                + "    <style>\n" + "        html {\n" + "            box-sizing: border-box;\n"
                + "            overflow: -moz-scrollbars-vertical;\n" + "            overflow-y: scroll;\n" + "        }\n" + "\n"
                + "        *,\n" + "        *:before,\n" + "        *:after {\n" + "            box-sizing: inherit;\n" + "        }\n"
                + "\n" + "        body {\n" + "            margin: 0;\n" + "            background: #fafafa;\n" + "        }\n"
                + "    </style>\n" + "</head>\n" + "\n" + "<body>\n"
                + "    <a href=\"https://github.com/requests/httpbin\" class=\"github-corner\" aria-label=\"View source on Github\">\n"
                + "        <svg width=\"80\" height=\"80\" viewBox=\"0 0 250 250\" style=\"fill:#151513; color:#fff; position: absolute; top: 0; border: 0; right: 0;\"\n"
                + "            aria-hidden=\"true\">\n"
                + "            <path d=\"M0,0 L115,115 L130,115 L142,142 L250,250 L250,0 Z\"></path>\n"
                + "            <path d=\"M128.3,109.0 C113.8,99.7 119.0,89.6 119.0,89.6 C122.0,82.7 120.5,78.6 120.5,78.6 C119.2,72.0 123.4,76.3 123.4,76.3 C127.3,80.9 125.5,87.3 125.5,87.3 C122.9,97.6 130.6,101.9 134.4,103.2\"\n"
                + "                fill=\"currentColor\" style=\"transform-origin: 130px 106px;\" class=\"octo-arm\"></path>\n"
                + "            <path d=\"M115.0,115.0 C114.9,115.1 118.7,116.5 119.8,115.4 L133.7,101.6 C136.9,99.2 139.9,98.4 142.2,98.6 C133.8,88.0 127.5,74.4 143.8,58.0 C148.5,53.4 154.0,51.2 159.7,51.0 C160.3,49.4 163.2,43.6 171.4,40.1 C171.4,40.1 176.1,42.5 178.8,56.2 C183.1,58.6 187.2,61.8 190.9,65.4 C194.5,69.0 197.7,73.2 200.1,77.6 C213.8,80.2 216.3,84.9 216.3,84.9 C212.7,93.1 206.9,96.0 205.4,96.6 C205.1,102.4 203.0,107.8 198.3,112.5 C181.9,128.9 168.3,122.5 157.7,114.1 C157.9,116.9 156.7,120.9 152.7,124.9 L141.0,136.5 C139.8,137.7 141.6,141.9 141.8,141.8 Z\"\n"
                + "                fill=\"currentColor\" class=\"octo-body\"></path>\n" + "        </svg>\n" + "    </a>\n"
                + "    <svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" style=\"position:absolute;width:0;height:0\">\n"
                + "        <defs>\n" + "            <symbol viewBox=\"0 0 20 20\" id=\"unlocked\">\n"
                + "                <path d=\"M15.8 8H14V5.6C14 2.703 12.665 1 10 1 7.334 1 6 2.703 6 5.6V6h2v-.801C8 3.754 8.797 3 10 3c1.203 0 2 .754 2 2.199V8H4c-.553 0-1 .646-1 1.199V17c0 .549.428 1.139.951 1.307l1.197.387C5.672 18.861 6.55 19 7.1 19h5.8c.549 0 1.428-.139 1.951-.307l1.196-.387c.524-.167.953-.757.953-1.306V9.199C17 8.646 16.352 8 15.8 8z\"></path>\n"
                + "            </symbol>\n" + "\n" + "            <symbol viewBox=\"0 0 20 20\" id=\"locked\">\n"
                + "                <path d=\"M15.8 8H14V5.6C14 2.703 12.665 1 10 1 7.334 1 6 2.703 6 5.6V8H4c-.553 0-1 .646-1 1.199V17c0 .549.428 1.139.951 1.307l1.197.387C5.672 18.861 6.55 19 7.1 19h5.8c.549 0 1.428-.139 1.951-.307l1.196-.387c.524-.167.953-.757.953-1.306V9.199C17 8.646 16.352 8 15.8 8zM12 8H8V5.199C8 3.754 8.797 3 10 3c1.203 0 2 .754 2 2.199V8z\"\n"
                + "                />\n" + "            </symbol>\n" + "\n" + "            <symbol viewBox=\"0 0 20 20\" id=\"close\">\n"
                + "                <path d=\"M14.348 14.849c-.469.469-1.229.469-1.697 0L10 11.819l-2.651 3.029c-.469.469-1.229.469-1.697 0-.469-.469-.469-1.229 0-1.697l2.758-3.15-2.759-3.152c-.469-.469-.469-1.228 0-1.697.469-.469 1.228-.469 1.697 0L10 8.183l2.651-3.031c.469-.469 1.228-.469 1.697 0 .469.469.469 1.229 0 1.697l-2.758 3.152 2.758 3.15c.469.469.469 1.229 0 1.698z\"\n"
                + "                />\n" + "            </symbol>\n" + "\n"
                + "            <symbol viewBox=\"0 0 20 20\" id=\"large-arrow\">\n"
                + "                <path d=\"M13.25 10L6.109 2.58c-.268-.27-.268-.707 0-.979.268-.27.701-.27.969 0l7.83 7.908c.268.271.268.709 0 .979l-7.83 7.908c-.268.271-.701.27-.969 0-.268-.269-.268-.707 0-.979L13.25 10z\"\n"
                + "                />\n" + "            </symbol>\n" + "\n"
                + "            <symbol viewBox=\"0 0 20 20\" id=\"large-arrow-down\">\n"
                + "                <path d=\"M17.418 6.109c.272-.268.709-.268.979 0s.271.701 0 .969l-7.908 7.83c-.27.268-.707.268-.979 0l-7.908-7.83c-.27-.268-.27-.701 0-.969.271-.268.709-.268.979 0L10 13.25l7.418-7.141z\"\n"
                + "                />\n" + "            </symbol>\n" + "\n" + "\n"
                + "            <symbol viewBox=\"0 0 24 24\" id=\"jump-to\">\n"
                + "                <path d=\"M19 7v4H5.83l3.58-3.59L8 6l-6 6 6 6 1.41-1.41L5.83 13H21V7z\" />\n" + "            </symbol>\n"
                + "\n" + "            <symbol viewBox=\"0 0 24 24\" id=\"expand\">\n"
                + "                <path d=\"M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z\" />\n" + "            </symbol>\n" + "\n"
                + "        </defs>\n" + "    </svg>\n" + "\n" + "\n" + "    <div id=\"swagger-ui\">\n"
                + "        <div data-reactroot=\"\" class=\"swagger-ui\">\n" + "            <div>\n"
                + "                <div class=\"information-container wrapper\">\n"
                + "                    <section class=\"block col-12\">\n" + "                        <div class=\"info\">\n"
                + "                            <hgroup class=\"main\">\n"
                + "                                <h2 class=\"title\">httpbin.org\n" + "                                    <small>\n"
                + "                                        <pre class=\"version\">0.9.2</pre>\n"
                + "                                    </small>\n" + "                                </h2>\n"
                + "                                <pre class=\"base-url\">[ Base URL: httpbin.org/ ]</pre>\n"
                + "                            </hgroup>\n" + "                            <div class=\"description\">\n"
                + "                                <div class=\"markdown\">\n"
                + "                                    <p>A simple HTTP Request &amp; Response Service.\n"
                + "                                        <br>\n" + "                                        <br>\n"
                + "                                        <b>Run locally: </b>\n"
                + "                                        <code>$ docker run -p 80:80 kennethreitz/httpbin</code>\n"
                + "                                    </p>\n" + "                                </div>\n"
                + "                            </div>\n" + "                            <div>\n" + "                                <div>\n"
                + "                                    <a href=\"https://kennethreitz.org\" target=\"_blank\">the developer - Website</a>\n"
                + "                                </div>\n"
                + "                                <a href=\"mailto:me@kennethreitz.org\">Send email to the developer</a>\n"
                + "                            </div>\n" + "                        </div>\n"
                + "                        <!-- ADDS THE LOADER SPINNER -->\n"
                + "                        <div class=\"loading-container\">\n"
                + "                            <div class=\"loading\"></div>\n" + "                        </div>\n" + "\n"
                + "                    </section>\n" + "                </div>\n" + "            </div>\n" + "        </div>\n"
                + "    </div>\n" + "\n" + "\n" + "    <div class='swagger-ui'>\n" + "        <div class=\"wrapper\">\n"
                + "            <section class=\"clear\">\n" + "                <span style=\"float: right;\">\n"
                + "                    [Powered by\n"
                + "                    <a target=\"_blank\" href=\"https://github.com/rochacbruno/flasgger\">Flasgger</a>]\n"
                + "                    <br>\n" + "                </span>\n" + "            </section>\n" + "        </div>\n"
                + "    </div>\n" + "\n" + "\n" + "\n" + "    <script src=\"/flasgger_static/swagger-ui-bundle.js\"> </script>\n"
                + "    <script src=\"/flasgger_static/swagger-ui-standalone-preset.js\"> </script>\n"
                + "    <script src='/flasgger_static/lib/jquery.min.js' type='text/javascript'></script>\n" + "    <script>\n" + "\n"
                + "        window.onload = function () {\n" + "            \n" + "\n" + "            fetch(\"/spec.json\")\n"
                + "                .then(function (response) {\n" + "                    response.json()\n"
                + "                        .then(function (json) {\n"
                + "                            var current_protocol = window.location.protocol.slice(0, -1);\n"
                + "                            if (json.schemes[0] != current_protocol) {\n"
                + "                                // Switches scheme to the current in use\n"
                + "                                var other_protocol = json.schemes[0];\n"
                + "                                json.schemes[0] = current_protocol;\n"
                + "                                json.schemes[1] = other_protocol;\n" + "\n" + "                            }\n"
                + "                            json.host = window.location.host;  // sets the current host\n" + "\n"
                + "                            const ui = SwaggerUIBundle({\n" + "                                spec: json,\n"
                + "                                validatorUrl: null,\n" + "                                dom_id: '#swagger-ui',\n"
                + "                                deepLinking: true,\n" + "                                jsonEditor: true,\n"
                + "                                docExpansion: \"none\",\n" + "                                apisSorter: \"alpha\",\n"
                + "                                //operationsSorter: \"alpha\",\n" + "                                presets: [\n"
                + "                                    SwaggerUIBundle.presets.apis,\n"
                + "                                    // yay ES6 modules ↘\n"
                + "                                    Array.isArray(SwaggerUIStandalonePreset) ? SwaggerUIStandalonePreset : SwaggerUIStandalonePreset.default\n"
                + "                                ],\n" + "                                plugins: [\n"
                + "                                    SwaggerUIBundle.plugins.DownloadUrl\n" + "                                ],\n"
                + "            \n" + "            // layout: \"StandaloneLayout\"  // uncomment to enable the green top header\n"
                + "        })\n" + "\n" + "        window.ui = ui\n" + "\n"
                + "        // uncomment to rename the top brand if layout is enabled\n"
                + "        // $(\".topbar-wrapper .link span\").replaceWith(\"<span>httpbin</span>\");\n" + "        })\n" + "    })\n"
                + "}\n" + "    </script>  <div class='swagger-ui'>\n" + "    <div class=\"wrapper\">\n"
                + "        <section class=\"block col-12 block-desktop col-12-desktop\">\n" + "            <div>\n" + "\n"
                + "                <h2>Other Utilities</h2>\n" + "\n" + "                <ul>\n" + "                    <li>\n"
                + "                        <a href=\"/forms/post\">HTML form</a> that posts to /post /forms/post</li>\n"
                + "                </ul>\n" + "\n" + "                <br />\n" + "                <br />\n" + "            </div>\n"
                + "        </section>\n" + "    </div>\n" + "</div>\n" + "</body>\n" + "\n" + "</html>";

        Matcher matcher = REPLACE_PATTERN.matcher(stringBody);
        String modifiedBody = matcher.replaceAll(NEW_BASE_PATH);
        System.out.println(modifiedBody);
    }
}
