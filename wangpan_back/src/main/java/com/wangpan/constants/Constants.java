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
    public static final String REDIS_KEY_SYS_SETTINGS="wangpan:syssetting:";  //easypan:syssetting:
    //用户使用空间
    public static final String REDIS_KEY_USERSPACE_USED="wangpan:user:spaceused:";  //
    public static final Integer REDIS_KEY_EXPIRES_MINUTE=60;
    public static final Long REDIS_KEY_EXPIRES_DAY=60*60*24L;
    public static final String REDIS_USER_FILE_TEMP_SIZE="wangpan:user:file:temp:";

    //空间单位：兆
    public static final Long MB=1024 *1024L;
    //默认使用空间
    public static final Integer userInitUseSpace=5;

    //登录
    public static final String SESSION_USER="session_user";

    public static final String FILE_PATH="/file/";
    //头像路径
    public static final String AVATAR_PATH=FILE_PATH+"avatar/";
    //默认头像
    public static final String AVATAR_DEFAULT="defaultAvatar.jpg";

    //缓存文件目录
    public static final String FOLD_TEMP="/temp/";

    //视频文件切割ts文件
    public static final String CMD_TRANSFER_2TS="ffmpeg -y -i %s -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
    public static final String CMD_CUT_TS="ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
    public static final String TS_NAME="index.tx";
    public static final String M3U8_NAME="index.m3u8";

}
