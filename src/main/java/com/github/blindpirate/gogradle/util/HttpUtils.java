package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.util.logging.ProgressMonitorInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET;

/**
 * Utils for http access.
 * To support mocking, it does not use public static method intentionally.
 */
public class HttpUtils {
    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";
    public static final String USER_AGENT = "user-agent";
    private static final int TEN_SECONDS = 10000;
    private static final int FOUR_KB = 4096;
    private static final int HTTP_INTERNAL_REDIRECTION = 307;

    public String get(String url) throws IOException {
        return get(url, null);
    }

    public String get(String url,
                      Map<String, String> headers) throws IOException {
        return fetch(GET_METHOD, url, null, headers);
    }

    /**
     * Append query parameters to given url
     *
     * @param url    Url as string
     * @param params Map with query parameters
     * @return url        Url with query parameters appended
     */
    public String appendQueryParams(String url,
                                    Map<String, String> params) {
        StringBuilder fullUrl = new StringBuilder(url);
        if (params != null) {
            boolean first = (url.indexOf('?') == -1);
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (first) {
                    fullUrl.append('?');
                    first = false;
                } else {
                    fullUrl.append('&');
                }
                try {
                    fullUrl.append(URLEncoder.encode(param.getKey(), DEFAULT_CHARSET)).append('=');
                    fullUrl.append(URLEncoder.encode(param.getValue(), DEFAULT_CHARSET));
                } catch (UnsupportedEncodingException e) {
                    // ok to ignore
                }
            }
        }

        return fullUrl.toString();
    }

    /**
     * Send a request
     *
     * @param method  HTTP method, for example "GET" or "POST"
     * @param url     Url as string
     * @param body    Request body as string
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException the exception thrown
     */
    private String fetch(String method, String url, String body,
                         Map<String, String> headers) throws IOException {
        try (InputStream is = fetchAsInputStream(method, url, body, headers)) {
            return IOUtils.toString(is);
        }
    }

    private InputStream fetchAsInputStream(String method, String url, String body,
                                           Map<String, String> headers) throws IOException {
        if (headers != null && headers.containsKey(USER_AGENT)) {
            System.setProperty("http.agent", headers.get(USER_AGENT));
        }
        // connection
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setConnectTimeout(TEN_SECONDS);
        conn.setReadTimeout(TEN_SECONDS);

        // method
        if (method != null) {
            conn.setRequestMethod(method);
        }

        // headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        // body
        if (body != null) {
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes(DEFAULT_CHARSET));
            os.flush();
            os.close();
        }

        // handle redirects
        if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
                || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                || conn.getResponseCode() == HTTP_INTERNAL_REDIRECTION) {
            String location = conn.getHeaderField("Location");
            return fetchAsInputStream(method, location, body, headers);
        }
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IllegalStateException("Error in accessing " + url
                    + ", http response code: " + conn.getResponseCode());
        }

        // response
        return conn.getInputStream();
    }

    public void download(String url, Path filePath) throws IOException {
        InputStream is = fetchAsInputStream(GET_METHOD, url, null, null);
        Files.copy(new ProgressMonitorInputStream(url, is), filePath, StandardCopyOption.REPLACE_EXISTING);
    }
}
