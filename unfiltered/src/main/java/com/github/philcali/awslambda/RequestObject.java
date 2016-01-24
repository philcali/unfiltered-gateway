package com.github.philcali.awslambda;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;

public class RequestObject {
    private Context context;
    private byte[] bytes;
    private String method = "GET";
    private String sourceIp = "0.0.0.0";
    private String charset = "UTF-8";
    private String resourcePath = "/";
    private String headerString;
    private String queryString;
    private String pathString;
    private String environmentString;
    private Map<String, Object> body;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private Map<String, String> pathParams;
    private Map<String, String> environment;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Optional<byte[]> getBytes() {
        return Optional.ofNullable(bytes);
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public Map<String, Object> getBody() {
        if (body == null) {
            return Collections.emptyMap();
        }
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        if (headers == null) {
            headers = splitString(headerString);
        }
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeaderString() {
        return headerString;
    }

    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }

    public Map<String, String> getPathParams() {
        if (pathParams == null) {
            pathParams = splitString(pathString);
        }
        return pathParams;
    }

    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }

    public String getPathString() {
        return pathString;
    }

    public void setPathString(String pathString) {
        this.pathString = pathString;
    }

    public Map<String, String> getQueryParams() {
        if (queryParams == null) {
            queryParams = splitString(queryString);
        }
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Map<String, String> getEnvironment() {
        if (environment == null) {
            environment = splitString(environmentString);
        }
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public String getEnvironmentString() {
        return environmentString;
    }

    public void setEnvironmentString(String environmentString) {
        this.environmentString = environmentString;
    }

    private Map<String, String> splitString(String string) {
        Map<String, String> map = new HashMap<>();
        if (!string.isEmpty() && !string.equals("{}")) {
            string = string.substring(1, string.length() - 1);
            String[] parts = string.split(",\\s*");
            Arrays.stream(parts).forEach((part) -> {
                String[] keyValue = part.split("=", 2);
                map.put(keyValue[0], keyValue[1]);
            });
        }
        return map;
    }
}
