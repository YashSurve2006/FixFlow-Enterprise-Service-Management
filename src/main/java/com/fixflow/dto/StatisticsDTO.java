package com.fixflow.dto;

import java.util.List;

public class StatisticsDTO {
    private long totalRequests;
    private long pending;
    private long assigned;
    private long inProgress;
    private long resolved;
    private long closed;
    private long cancelled;
    private long urgent;
    private List<CategoryStatsDTO> requestsByCategory;

    public long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }

    public long getAssigned() { return assigned; }
    public void setAssigned(long assigned) { this.assigned = assigned; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getResolved() { return resolved; }
    public void setResolved(long resolved) { this.resolved = resolved; }

    public long getClosed() { return closed; }
    public void setClosed(long closed) { this.closed = closed; }

    public long getCancelled() { return cancelled; }
    public void setCancelled(long cancelled) { this.cancelled = cancelled; }

    public long getUrgent() { return urgent; }
    public void setUrgent(long urgent) { this.urgent = urgent; }

    public List<CategoryStatsDTO> getRequestsByCategory() { return requestsByCategory; }
    public void setRequestsByCategory(List<CategoryStatsDTO> requestsByCategory) { this.requestsByCategory = requestsByCategory; }
}
