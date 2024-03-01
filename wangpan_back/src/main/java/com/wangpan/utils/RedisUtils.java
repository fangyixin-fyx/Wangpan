package com.wangpan.utils;

import com.wangpan.constants.Constants;
import com.wangpan.dto.SysSettingsDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.FileMapper;
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
    @Autowired
    private FileMapper fileMapper;
    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);
    private static final String LOCK_KEY_SPACE="space_";

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

    public UserSpaceDto getUsedSpaceDto(String uid){
        UserSpaceDto userSpaceDto=(UserSpaceDto)get(Constants.REDIS_KEY_USERSPACE_USED+uid);
        if(userSpaceDto==null){
            userSpaceDto=new UserSpaceDto();
            userSpaceDto.setUseSpace(fileMapper.getUsedSpaceByUid(uid));
            userSpaceDto.setTotalSpace(Constants.userInitUseSpace*Constants.MB);
            setByTime(Constants.REDIS_KEY_USERSPACE_USED+uid,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
        }
        return userSpaceDto;
    }

    public void setUserSpace(String userId,Long addSize){
        String key=Constants.REDIS_KEY_USERSPACE_USED+userId;
        String lock_key=LOCK_KEY_SPACE+key;
        boolean done=false;
        int count=0;
        while(!done){
            // 获取分布式锁，设置锁的过期时间为10s
            // SETNX命令，如果键不存在则设置值，设置成功返回 true，否则返回 false
            try {
                Boolean isLocked=redisTemplate.opsForValue().setIfAbsent(lock_key,"locked",10,TimeUnit.SECONDS);
                //获取锁成功
                if(isLocked!=null&&isLocked){
                    try{
                        UserSpaceDto userSpaceDto=getUsedSpaceDto(userId);
                        userSpaceDto.setUseSpace(userSpaceDto.getUseSpace()+addSize);
                        redisTemplate.opsForValue().set(key,userSpaceDto);
                        done=true;
                    }finally {
                        delete(lock_key);
                    }
                }else{
                    //获取锁失败，等待3s后重试
                    Thread.sleep(3000);
                    if (++count>3){
                        logger.info(userId+"用户获取redis分布式锁失败，使用空间数据更新失败");
                        throw new BusinessException("使用空间数据并发更新失败，请重新上传文件");
                    }
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e){
                throw new BusinessException(e.getMessage(),e);
            }
        }
    }
}
