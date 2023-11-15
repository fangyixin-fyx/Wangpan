package com.wangpan.exception;

import com.wangpan.enums.ResponseCodeEnum;

public class BusinessException extends RuntimeException {
    private ResponseCodeEnum codeEnum;
    private Integer code;
    private String message;

    public BusinessException(String message, Throwable e){
        super(message,e);
        this.message=message;
    }

    public BusinessException(String message){
        super(message);
        this.message=message;
    }

    public BusinessException(Throwable e){
        super(e);
    }

    public BusinessException(ResponseCodeEnum codeEnum){
        super(codeEnum.getMsg());
        this.codeEnum=codeEnum;
        this.code=codeEnum.getCode();
        this.message=codeEnum.getMsg();
    }

    public BusinessException(Integer code, String message){
        super(message);
        this.message=message;
        this.code=code;
    }

    public ResponseCodeEnum getCodeEnum(){ return this.codeEnum; }

    public Integer getCode(){return this.code;}

    @Override
    public String getMessage() {return this.message;}

    /**
     * fillInStackTrace 业务异常不需要堆栈信息，提高效率
     */
    @Override
    public Throwable fillInStackTrace(){ return this; }
}
