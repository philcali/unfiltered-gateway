package com.github.philcali.awslambda;

import java.util.Map;

public class MessageObject {
    private int code;
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
