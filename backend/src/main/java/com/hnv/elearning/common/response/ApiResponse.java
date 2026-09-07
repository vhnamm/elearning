package com.hnv.elearning.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApiResponse<T> {
    private boolean success = true;
    private int code;
    private String message;
    private T data;
    private Object errors;
}
