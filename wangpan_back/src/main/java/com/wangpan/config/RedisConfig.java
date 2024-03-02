package com.wangpan.config;

import com.wangpan.constants.Constants;
import com.wangpan.dto.SysSettingsDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.utils.RedisUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis配置信息
 * @author fangyixin
 * @date 2023/11/14 16:35
 */
@Configuration
public class RedisConfig<V> {

    //序列化
    @Bean
    public RedisTemplate<String, V> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, V> template=new RedisTemplate<String, V>();
        template.setConnectionFactory(factory);
        //设置key序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置value序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }

}
