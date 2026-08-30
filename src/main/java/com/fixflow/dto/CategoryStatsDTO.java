package com.fixflow.dto;

public class CategoryStatsDTO {
    private String categoryName;
    private long count;

    public CategoryStatsDTO() {}

    public CategoryStatsDTO(String categoryName, long count) {
        this.categoryName = categoryName;
        this.count = count;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
