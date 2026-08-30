package com.fixflow.dto;

public class DataResponse<T> {
    private T data;

    public DataResponse() {}

    public DataResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
