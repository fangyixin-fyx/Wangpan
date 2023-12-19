package com.wangpan.utils;

import com.wangpan.constants.Constants;
import com.wangpan.dto.SysSettingsDto;
import com.wangpan.dto.UserSpaceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 操作redis
 * @author fangyixin
 * @date 2023/11/14 16:17
 */
@Component("redisUtils")
public class RedisUtils {
    @Resource
    private RedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 删除缓存
     */
    public void delete(String key){
        if(key!=null){
            redisTemplate.delete(key);
        }
    }


    /**
     * 获取一个值
     * @param key
     * @return
     */
    /*
    public V get(String key){
        return key==null ? null : redisTemplate.opsForValue().get(key);
    }

     */
    public Object get(String key){
        return key==null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 存进redis
     * @param key
     * @param value
     * @return
     */
    public <V> boolean set(String key, V value){
        try{
            redisTemplate.opsForValue().set(key, value);
            return true;
        }catch (Exception e){
            logger.error("设置redis key:{}, value:{}失败",key,value);
            return false;
        }
    }

    /**
     * 存入k-v，并设置有效期
     */
    public <V> boolean setByTime(String key, V value, Long time){
        try{
            if(time>0)  redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            else set(key, value);
            return true;
        }catch (Exception e){
            logger.error("设置redis key:{}, value:{}失败",key,value);
            return false;
        }
    }


    /**
     * 累加空间
     * key: Constants.REDIS_USER_FILE_TEMP_SIZE + userId + fileId
     */
    public void setFileTempSize(String uid,String fid,Long fileSize){
        String key=Constants.REDIS_USER_FILE_TEMP_SIZE+uid+fid;
        //获取临时文件夹空间使用情况
        Long currentSize=getFileSizeFromRedis(key);
        Long totalSize=currentSize+fileSize;
        setByTime(key,totalSize,Constants.REDIS_KEY_EXPIRES_DAY);
    }

    /**
     * 获取临时文件夹空间使用情况
     * key: Constants.REDIS_USER_FILE_TEMP_SIZE + userId + fileId
     */
    public Long getFileSizeFromRedis(String key){
        Object size=redisTemplate.opsForValue().get(key);
        if(size instanceof Integer){
            return ((Integer)size).longValue();
        }else if(size instanceof Long){
            return (Long)size;
        }
        return 0L;
    }
}
