package com.wangpan.config;

import com.wangpan.constants.Constants;
import com.wangpan.dto.SysSettingsDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.mapper.FileMapper;
import com.wangpan.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author fangyixin
 * @date 2023/11/20 22:01
 */
@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private FileMapper fileMapper;

    //可改进
    public SysSettingsDto getSysSettingDto(){
        //根据key获取value
        SysSettingsDto sysSettingsDto= (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTINGS);
        if(sysSettingsDto==null) {
            sysSettingsDto=new SysSettingsDto(); //有初始值
            //同时往redis里存入，把邮箱格式等默认数据存进redis里
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTINGS, sysSettingsDto);
        }
        return sysSettingsDto;
    }

    //将用户空间使用情况存入redis中
    public void saveUserSpaceUsed(String uid, UserSpaceDto userSpaceDto){
        redisUtils.setByTime(Constants.REDIS_KEY_USERSPACE_USED+uid,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
    }

    public UserSpaceDto getUsedSpaceDto(String uid){
        UserSpaceDto userSpaceDto=(UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USERSPACE_USED+uid);
        if(userSpaceDto==null){
            userSpaceDto=new UserSpaceDto();
            userSpaceDto.setUseSpace(fileMapper.getUsedSpaceByUid(uid));
            userSpaceDto.setTotalSpace(Constants.userInitUseSpace*Constants.MB);
            saveUserSpaceUsed(uid,userSpaceDto);
        }
        return userSpaceDto;
    }


    /**
     * 获取临时文件夹空间使用情况
     */
    public Long getFileSizeFromRedis(String key){
        Object size=redisUtils.get(key);
        if(size instanceof Integer){
            return ((Integer)size).longValue();
        }else if(size instanceof Long){
            return (Long)size;
        }
        return 0L;
    }

    public void setFileTempSize(String uid,String fid,Long fileSize){
        String key=Constants.REDIS_USER_FILE_TEMP_SIZE+uid+fid;
        Long currentSize=getFileSizeFromRedis(key);
        redisUtils.setByTime(key,currentSize+fileSize,Constants.REDIS_KEY_EXPIRES_DAY);
    }
}
