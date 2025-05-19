package io.github.kemblekaran.order_service.response;

import lombok.Builder;

@Builder
public class ApiResponse<T> {

    private int statusCode;
    private T data;
    private T error;
    private String message;
}
