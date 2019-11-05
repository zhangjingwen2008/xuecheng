package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

public class CustomException extends RuntimeException {

    private ResultCode resultCode;

    public ResultCode getResultCode() {
        return resultCode;
    }

    public CustomException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }
}
