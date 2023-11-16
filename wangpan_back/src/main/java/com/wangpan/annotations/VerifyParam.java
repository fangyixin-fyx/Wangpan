package com.wangpan.annotations;

import com.wangpan.enums.VerifyRegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验参数自定义注解
 * @author fangyixin
 * @date 2023/11/16 13:29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.PARAMETER})
public @interface VerifyParam {

    int min() default -1; //校验最小值

    int max() default -1;

    boolean required() default false;   //是否闭环

    VerifyRegexEnum regex() default VerifyRegexEnum.NO;  //默认不进行正则校验
}
