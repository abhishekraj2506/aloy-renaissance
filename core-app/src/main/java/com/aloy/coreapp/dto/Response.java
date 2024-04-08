package com.aloy.coreapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private T response = null;
    private boolean success;
    private int statusCode;
    private String error = null;

    public Response(boolean success, String error) {
        this.success = success;
        this.error = error;
        this.statusCode = 500;
    }

    public Response(boolean success, int statusCode, String error) {
        this.success = success;
        this.statusCode = statusCode;
        this.error = error;
    }

    public Response(boolean success) {
        this.success = success;
        this.statusCode = 200;
        this.error = null;
    }

    public Response(T response) {
        this.response = response;
        this.statusCode = 200;
        this.success = true;
    }
}