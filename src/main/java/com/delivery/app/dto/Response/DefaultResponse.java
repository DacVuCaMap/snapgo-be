package com.delivery.app.dto.Response;

import lombok.Data;

@Data
public class DefaultResponse {
    private Integer status;
    private String message;
    private Object value;
    private boolean success;

    public DefaultResponse(Integer status,String message){
        this.message=message;
        this.status=status;
    }
    public DefaultResponse(Integer status,String message,boolean success){
        this.message=message;
        this.status=status;
        this.success=success;
    }
    public DefaultResponse(Integer status,String message, Object value,boolean success) {
        this.status = status;
        this.message = message;
        this.value = value;
        this.success = success;
    }
    public DefaultResponse(String message, Object value,boolean success) {
        this.message = message;
        this.value = value;
        this.success = success;
    }
}
