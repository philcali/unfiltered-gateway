package com.github.philcali.awslambda;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;

public class ResponseObject {
    private int status = 200;
    private boolean raw = false;
    private Map<String, String> headers;
    private ByteArrayOutputStream outputStream;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isRaw() {
        return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    public Map<String, String> getHeaders() {
        if (headers == null) {
            headers = Collections.emptyMap();
        }
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public ByteArrayOutputStream getOutputStream() {
        if (outputStream == null) {
            outputStream = new ByteArrayOutputStream();
        }
        return outputStream;
    }

    public void setOutputStream(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
