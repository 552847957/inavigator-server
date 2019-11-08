package ru.sberbank.qlik.sense.methods;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponse<R> {
    private float jsonrpc;
    private String method;
    private Object error;
    private Integer id;
    public R result;
    private Object params;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public R getResult() {
        return result;
    }

    public void setResult(R result) {
        this.result = result;
    }

    public float getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(float jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
