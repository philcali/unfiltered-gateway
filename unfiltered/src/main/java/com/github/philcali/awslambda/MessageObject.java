package com.github.philcali.awslambda;

import java.util.Map;

public class MessageObject {
    private int code;
    private Map<String, String> headers;
    private Map<String, Object> body;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public MessageObject withCode(int code) {
        setCode(code);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public MessageObject withHeaders(Map<String, String> headers) {
        setHeaders(headers);
        return this;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    public MessageObject withBody(Map<String, Object> body) {
        setBody(body);
        return this;
    }
}
