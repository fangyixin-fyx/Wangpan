package com.wangpan.enums;

/**
 * 常用正则的枚举
 * @author fangyixin
 * @date 2023/11/16 13:34
 */
public enum VerifyRegexEnum {

    NO("","不校验"),
    EMAIL("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$","邮箱"),
    PASSWORD("^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,}$","只能是数字，字母，特殊字符，8-18位");

    private String regex;
    private String desc; //描述信息

    VerifyRegexEnum(String regex,String desc){
        this.regex=regex;
        this.desc=desc;
    }

    public String getRegex() {
        return regex;
    }

    public String getDesc() {
        return desc;
    }
}
