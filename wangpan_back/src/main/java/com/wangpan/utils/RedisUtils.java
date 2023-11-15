package com.wangpan.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 操作redis
 * @author fangyixin
 * @date 2023/11/14 16:17
 */
@Component
public class RedisUtils<V> {
    @Resource
    private RedisTemplate<String, V> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 删除缓存
     */
    /*
    public void delete(String... key){
        if(key!=null && key.length>0){
            if(key.length==1) redisTemplate.delete(key[0]);
            else redisTemplate.delete((Collection<String>) CollectionsUtils.arrayToList(key));
        }
    }

     */

    /**
     * 获取一个值
     * @param key
     * @return
     */
    public V get(String key){
        return key==null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 存进redis
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, V value){
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
    public boolean setByTime(String key, V value, Long time){
        try{
            if(time>0)  redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            else set(key, value);
            return true;
        }catch (Exception e){
            logger.error("设置redis key:{}, value:{}失败",key,value);
            return false;
        }
    }

}
