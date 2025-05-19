package io.github.kemblekaran.orderservice.response;

import lombok.Builder;

@Builder
public class ApiResponse<T> {

    private int statusCode;
    private T data;
    private T error;
    private String message;
}
