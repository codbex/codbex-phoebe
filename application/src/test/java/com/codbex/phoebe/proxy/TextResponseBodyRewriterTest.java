package com.codbex.phoebe.proxy;

import com.codbex.phoebe.cfg.AppConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TextResponseBodyRewriterTest {

    @InjectMocks
    private TextResponseBodyRewriter rewriter;

    @Mock
    private ServerRequest request;

    @Mock
    private ServerResponse response;

    @Mock
    private HttpHeaders responseHeaders;

    @BeforeAll
    public static void setUp() {
        AppConfig.AIRFLOW_URL.setValues("http://localhost:8080");
    }

    @Test
    void apply_shouldRewriteBody_whenContentTypeIsTextHtml() {
        byte[] originalBody = """
                    <html><body><a href="/test/path">link</a></body></html>
                    <html><body><a href='/test/path'>link</a></body></html>
                    <script src="/static/appbuilder/js/jquery-latest.js" nonce=""></script>
                    <script src='/static/appbuilder/js/jquery-latest.js' nonce=""></script>
                    <meta name="dags_index" content="/home">
                    <meta name="dags_index" content='/home'>
                    fetch("/spec.json")
                    fetch('/spec.json')
                    http://localhost:8080/some/aiflow/path
                    http://localhost:8080
                    http://localhost:8080/services/airflow/some/other/path
                """.getBytes(StandardCharsets.UTF_8);

        byte[] expectedBody = """
                    <html><body><a href="/services/airflow/test/path">link</a></body></html>
                    <html><body><a href='/services/airflow/test/path'>link</a></body></html>
                    <script src="/services/airflow/static/appbuilder/js/jquery-latest.js" nonce=""></script>
                    <script src='/services/airflow/static/appbuilder/js/jquery-latest.js' nonce=""></script>
                    <meta name="dags_index" content="/services/airflow/home">
                    <meta name="dags_index" content='/services/airflow/home'>
                    fetch("/services/airflow/spec.json")
                    fetch('/services/airflow/spec.json')
                    http://localhost:80/services/airflow/some/aiflow/path
                    http://localhost:80/services/airflow
                    http://localhost:80/services/airflow/some/other/path
                """.getBytes(StandardCharsets.UTF_8);

        when(request.uri()).thenReturn(URI.create("http://localhost:80"));

        when(responseHeaders.getContentType()).thenReturn(MediaType.TEXT_HTML);
        when(response.headers()).thenReturn(responseHeaders);

        byte[] result = rewriter.apply(request, response, originalBody);

        assertThat(new String(result)).isEqualTo(new String(expectedBody));
    }

    @Test
    void apply_shouldNotRewriteBody_whenContentTypeIsNotText() {
        byte[] originalBody = "<html><body><a href=\"/test\">link</a></body></html>".getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        when(response.headers()).thenReturn(headers);

        byte[] result = rewriter.apply(request, response, originalBody);

        assertArrayEquals(originalBody, result);
    }

    @Test
    void rewriteBody_shouldReplaceAirflowUrl_whenHostHeaderIsPresent() {
        byte[] body = "<html><body>http://localhost:8080</body></html>".getBytes(StandardCharsets.UTF_8);
        String expectedBody = "<html><body>http://localhost:8080/services/airflow</body></html>";

        when(request.uri()).thenReturn(URI.create("http://localhost:8080/services/airflow"));

        String result = rewriter.rewriteBody(request, body);

        assertEquals(expectedBody, result);
    }

    @Test
    void rewriteBody_shouldNotReplaceAirflowUrl_whenHostHeaderIsMissing() {
        byte[] body = "<html><body>http://old-airflow-url</body></html>".getBytes(StandardCharsets.UTF_8);
        String expectedBody = "<html><body>http://old-airflow-url</body></html>";

        when(request.uri()).thenReturn(URI.create("http://localhost:8080/services/airflow"));

        String result = rewriter.rewriteBody(request, body);

        assertEquals(expectedBody, result);
    }

    private byte[] compressGzip(byte[] data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Test
    void getBody_shouldReturnOriginalBody_whenNotGzipped() {
        byte[] originalBody = "<html><body>content</body></html>".getBytes(StandardCharsets.UTF_8);

        HttpHeaders responseHeaders = new HttpHeaders();
        when(response.headers()).thenReturn(responseHeaders);

        byte[] result = rewriter.apply(request, response, originalBody);

        assertArrayEquals(originalBody, result);
    }
}
