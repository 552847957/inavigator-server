package ru.sberbank.qlik.sense.methods;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

public abstract class BaseRequest<R> {
    private String method;
    private Integer handle = -1;
    private Object params;
    private boolean delta;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer outKey;
    private Integer id;
    @JsonIgnore
    private boolean async = true;
    @JsonIgnore
    private R response = null;
    @JsonIgnore
    private final Class<R> responseClazz;

    public BaseRequest(String method, Class<R> clazz) {
        this.responseClazz = clazz;
        this.method = method;
    }

    public boolean isDelta() {
        return delta;
    }

    public void setDelta(boolean delta) {
        this.delta = delta;
    }

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

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public R getResponse() {
        return response;
    }

    public void setResponse(R response) {
        this.response = response;
    }

    public Integer getHandle() {
        return handle;
    }

    public void setHandle(Integer handle) {
        this.handle = handle;
    }

    public Integer getOutKey() {
        return outKey;
    }

    public void setOutKey(Integer outKey) {
        this.outKey = outKey;
    }

    @JsonIgnore
    public Class<R> getResponseType() {
        return responseClazz;
    }
}
