package com.wangpan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fangyixin
 * @date 2023/11/14 16:50
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) //往缓存改对象不会报错，如果javabean数据和redis数据不一致会报错
public class SysSettingsDto implements Serializable {
    //设置邮箱验证码默认格式
    private String registerEmailTitle="邮箱验证码";
    private String registerEmailContent="你好，你的邮箱验证码是：%s, 15分钟内有效。";
    //网盘初始化空间大小
    private Integer userInitUseSpace=5;
}
