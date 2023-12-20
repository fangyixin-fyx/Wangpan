package com.wangpan.enums;

public enum ResponseCodeEnum {
    CODE_200(200,"请求成功"),
    CODE_404(404,"请求地址不存在"),
    CODE_600(600,"请求参数错误"),
    CODE_601(601,"信息已存在"),
    CODE_500(500,"服务器返回错误"),
    CODE_901(901,"登陆数据有误或超时，请重新登录"),
    CODE_902(902,"分享链接不存在或已失效"),
    CODE_903(903,"分享验证失效，请重新验证"),
    CODE_FILE(501,"空间不足，上传文件失败");

    private Integer code;
    private String msg;

    ResponseCodeEnum(Integer code, String msg){
        this.code=code;
        this.msg=msg;
    }

    public Integer getCode(){ return this.code; }
    public String getMsg(){ return this.msg; }
}
