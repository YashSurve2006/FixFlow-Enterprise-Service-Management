package com.fixflow.dto;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> data;
    private PaginationMeta pagination;

    public PaginatedResponse() {}

    public PaginatedResponse(List<T> data, PaginationMeta pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public PaginationMeta getPagination() {
        return pagination;
    }

    public void setPagination(PaginationMeta pagination) {
        this.pagination = pagination;
    }
}
