package com.wangpan.constants;

/**
 * @author fangyixin
 * @date 2023/8/11 17:15
 */
//使用常量
public class Constants {

    public static final String CHECK_CODE_KEY="check_code_key";
    public static final String CHECK_CODE_KEY_EMAIL="check_code_key_email";
    public static final Integer LENGTH_5=5;
    public static final Integer LENGTH_10=10;
    public static final Integer ZERO=0;
    //验证码有效时间
    public static final Integer LENGTH_15=15;

    //redis相关
    public static final String REDIS_KEY_SYS_SETTINGS="wangpan:syssetting:";
    //用户使用空间
    public static final String REDIS_KEY_USERSPACE_USED="wangpan:user:spaceused:";
    public static final Integer REDIS_KEY_EXPIRES_MINUTE=60;
    public static final Long REDIS_KEY_EXPIRES_DAY=60*60*24L;

    //空间单位：兆
    public static final Long MB=1024 *1024L;

    //登录
    public static final String SESSION_USER="session_user";



}
