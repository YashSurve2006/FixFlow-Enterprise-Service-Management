package com.fixflow.dto;

import com.fixflow.model.RequestStatus;

public class UpdateStatusRequest {
    private RequestStatus status;

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
}
