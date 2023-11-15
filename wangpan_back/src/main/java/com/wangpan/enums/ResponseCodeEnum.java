package com.wangpan.enums;

public enum ResponseCodeEnum {
    CODE_200(200,"请求成功"),
    CODE_404(404,"请求地址不存在"),
    CODE_600(600,"请求参数错误"),
    CODE_601(601,"信息已存在"),
    CODE_500(500,"服务器返回错误");

    private Integer code;
    private String msg;

    ResponseCodeEnum(Integer code, String msg){
        this.code=code;
        this.msg=msg;
    }

    public Integer getCode(){ return this.code; }
    public String getMsg(){ return this.msg; }
}
