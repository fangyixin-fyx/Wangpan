package com.wangpan.annotations;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * 自定义全局拦截器
 * @author fangyixin
 * @date 2023/11/16 13:26
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface GlobalInterceptor {
    /**
     * 是否校验参数
     */
    boolean checkParam() default false;

    /**
     * 校验登录
     */
    boolean checkLogin() default true;

    boolean checkAdmin() default false;
}
