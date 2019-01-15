package com.github.blindpirate.gogradle.util.http;

import com.github.blindpirate.gogradle.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class HttpResponse {
    private String url;
    private int responseCode;
    private InputStream responseStream;

    private HttpResponse(String url, int responseCode, InputStream responseStream) {
        this.url = url;
        this.responseCode = responseCode;
        this.responseStream = responseStream;
    }

    public static HttpResponse of(String url, int responseCode, InputStream inputStream) {
        return new HttpResponse(url, responseCode, inputStream);
    }

    public int getResponseCode() {
        return responseCode;
    }

    public InputStream getResponseStream() {
        return responseStream;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public void checkValidity() throws IOException {
        if (responseCode < 200 || responseCode > 300) {
            throw new IOException("Error in accessing " + url
                    + ", http response code: " + responseCode);
        }
    }

    public String readHtml() throws IOException {
        try (InputStream is = responseStream) {
            return IOUtils.toString(is);
        }
    }
}
