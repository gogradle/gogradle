package com.github.blindpirate.gogradle.util;

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

    /**
     * Send a get request
     *
     * @param url
     * @return response
     * @throws IOException
     */
    public String get(String url) throws IOException {
        return get(url, null);
    }

    /**
     * Send a get request
     *
     * @param url     Url as string
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    public String get(String url,
                      Map<String, String> headers) throws IOException {
        return fetch(GET_METHOD, url, null, headers);
    }

    /**
     * Send a post request
     *
     * @param url     Url as string
     * @param body    Request body as string
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    public String post(String url, String body,
                       Map<String, String> headers) throws IOException {
        return fetch("POST", url, body, headers);
    }

    /**
     * Send a post request
     *
     * @param url  Url as string
     * @param body Request body as string
     * @return response   Response as string
     * @throws IOException
     */
    public String post(String url, String body) throws IOException {
        return post(url, body, null);
    }

    /**
     * Post a form with parameters
     *
     * @param url    Url as string
     * @param params map with parameters/values
     * @return response   Response as string
     * @throws IOException
     */
    public String postForm(String url, Map<String, String> params)
            throws IOException {
        return postForm(url, params, null);
    }

    /**
     * Post a form with parameters
     *
     * @param url     Url as string
     * @param params  Map with parameters/values
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    public String postForm(String url, Map<String, String> params,
                           Map<String, String> headers) throws IOException {
        // set content type
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        // parse parameters
        StringBuilder body = new StringBuilder();
        if (params != null) {
            boolean first = true;
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    body.append("&");
                }
                String value = param.getValue();
                body.append(URLEncoder.encode(param.getKey(), DEFAULT_CHARSET) + "=");
                body.append(URLEncoder.encode(value, DEFAULT_CHARSET));
            }
        }

        return post(url, body.toString(), headers);
    }

    /**
     * Send a put request
     *
     * @param url     Url as string
     * @param body    Request body as string
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    public String put(String url, String body,
                      Map<String, String> headers) throws IOException {
        return fetch("PUT", url, body, headers);
    }

    /**
     * Send a put request
     *
     * @param url Url as string
     * @return response   Response as string
     * @throws IOException
     */
    public String put(String url, String body) throws IOException {
        return put(url, body, null);
    }

    /**
     * Send a delete request
     *
     * @param url     Url as string
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    public String delete(String url,
                         Map<String, String> headers) throws IOException {
        return fetch("DELETE", url, null, headers);
    }

    /**
     * Send a delete request
     *
     * @param url Url as string
     * @return response   Response as string
     * @throws IOException
     */
    public String delete(String url) throws IOException {
        return delete(url, null);
    }

    /**
     * Append query parameters to given url
     *
     * @param url    Url as string
     * @param params Map with query parameters
     * @return url        Url with query parameters appended
     * @throws IOException
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
     * Retrieve the query parameters from given url
     *
     * @param url Url containing query parameters
     * @return params     Map with query parameters
     * @throws IOException
     */
    public Map<String, String> getQueryParams(String url)
            throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        int start = url.indexOf('?');
        while (start != -1) {
            // read parameter name
            int equals = url.indexOf('=', start);
            String param = "";
            if (equals != -1) {
                param = url.substring(start + 1, equals);
            } else {
                param = url.substring(start + 1);
            }

            // read parameter value
            String value = "";
            if (equals != -1) {
                start = url.indexOf('&', equals);
                if (start != -1) {
                    value = url.substring(equals + 1, start);
                } else {
                    value = url.substring(equals + 1);
                }
            }

            params.put(URLDecoder.decode(param, DEFAULT_CHARSET),
                    URLDecoder.decode(value, DEFAULT_CHARSET));
        }

        return params;
    }

    /**
     * Returns the url without query parameters
     *
     * @param url Url containing query parameters
     * @return url        Url without query parameters
     * @throws IOException
     */
    public String removeQueryParams(String url)
            throws IOException {
        int q = url.indexOf('?');
        if (q != -1) {
            return url.substring(0, q);
        } else {
            return url;
        }
    }

    /**
     * Send a request
     *
     * @param method  HTTP method, for example "GET" or "POST"
     * @param url     Url as string
     * @param body    Request body as string
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException
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
        Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
    }
}
