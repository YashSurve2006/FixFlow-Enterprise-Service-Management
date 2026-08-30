package com.fixflow.dto;

public class PaginationMeta {
    private int page;
    private int limit;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PaginationMeta() {}

    public PaginationMeta(int page, int limit, long totalItems) {
        this.page = page;
        this.limit = limit;
        this.totalItems = totalItems;
        this.totalPages = limit > 0 ? (int) Math.ceil((double) totalItems / limit) : 0;
        this.hasNext = page < totalPages;
        this.hasPrevious = page > 1;
    }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
}
