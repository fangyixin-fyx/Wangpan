package com.wangpan.config;

import com.wangpan.constants.Constants;
import com.wangpan.dto.SysSettingsDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * redis配置信息
 * @author fangyixin
 * @date 2023/11/14 16:35
 */
@Configuration
public class RedisConfig {
    @Autowired
    private RedisUtils redisUtils;

    public SysSettingsDto getSysSettingDto(){
        //根据key获取value
        SysSettingsDto sysSettingsDto= (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTINGS);
        if(sysSettingsDto==null) {
            sysSettingsDto=new SysSettingsDto(); //有初始值
            //同时往redis里存入，把邮箱格式等默认数据存进redis里
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTINGS,sysSettingsDto);
        }
        return sysSettingsDto;
    }

    //将用户空间使用情况存入redis中
    public void saveUserSpaceUsed(String uid, UserSpaceDto userSpaceDto){
        redisUtils.setByTime(Constants.REDIS_KEY_USERSPACE_USED+uid,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
    }

}
