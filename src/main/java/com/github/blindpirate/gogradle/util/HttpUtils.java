/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.util.http.HttpResponse;
import com.github.blindpirate.gogradle.util.logging.ProgressMonitorInputStream;
import org.gradle.api.logging.Logging;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET;

/**
 * Utils for http access.
 * To support mocking, it does not use public static method intentionally.
 */
public class HttpUtils {
    private static final Logger LOGGER = Logging.getLogger(HttpUtils.class);
    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";
    public static final String USER_AGENT = "user-agent";
    private static final int TEN_SECONDS = 10000;
    private static final int FOUR_KB = 4096;
    private static final int HTTP_INTERNAL_REDIRECTION = 307;

    /**
     * Perform a HTTP request and get response html string.
     * Expect 2xx response code, otherwise throw IOException.
     *
     * @param url the url
     * @return the response html
     * @throws IOException if response code is not 2xx
     */
    public String get(String url) throws IOException {
        return get(url, null);
    }

    public String get(String url, Map<String, String> headers) throws IOException {
        return fetch(GET_METHOD, url, null, headers);
    }

    public HttpResponse getResponse(String url, Map<String, String> headers) throws IOException {
        return fetchHttpResponse(GET_METHOD, url, null, headers);
    }

    /**
     * Append query parameters to given url
     *
     * @param url Url as string
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
     * @param method HTTP method, for example "GET" or "POST"
     * @param url Url as string
     * @param body Request body as string
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException the exception thrown
     */
    private String fetch(String method, String url, String body,
                         Map<String, String> headers) throws IOException {
        HttpResponse response = fetchHttpResponse(method, url, body, headers);
        response.checkValidity();
        return response.readHtml();
    }

    private InputStream fetchAsInputStream(String method, String url, String body,
                                           Map<String, String> headers) throws IOException {
        HttpResponse response = fetchHttpResponse(method, url, body, headers);
        response.checkValidity();
        return response.getResponseStream();
    }

    private HttpResponse fetchHttpResponse(String method, String url, String body,
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
            return fetchHttpResponse(method, location, body, headers);
        }

        try {
            return HttpResponse.of(url, conn.getResponseCode(), conn.getInputStream());
        } catch (IOException e) {
            LOGGER.debug("Error when accessing {} with {}", url, headers, e);
            return HttpResponse.of(url, conn.getResponseCode(), conn.getErrorStream());
        }

    }

    public void download(String url, Path filePath) throws IOException {
        InputStream is = fetchAsInputStream(GET_METHOD, url, null, null);
        Files.copy(new ProgressMonitorInputStream(url, is), filePath, StandardCopyOption.REPLACE_EXISTING);
    }
}
