package com.wangpan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wangpan.constants.Constants;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author fangyixin
 * @date 2023/11/14 16:50
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) //往缓存改对象不会报错，如果javabean数据和redis数据不一致会报错
@Component
public class SysSettingsDto implements Serializable {
    //设置邮箱验证码默认格式
    private String registerEmailTitle;
    private String registerEmailContent;
    //网盘初始化空间大小
    private Integer userInitUseSpace;

    public SysSettingsDto() {
        this.registerEmailTitle = Constants.EMAIL_TITLE;
        this.registerEmailContent = Constants.EMAIL_CONTEXT;
        this.userInitUseSpace = Constants.userInitUseSpace;
    }

}
