package com.wangpan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author fangyixin
 * @date 2023/8/6 19:15
 */
@EnableAsync //异步调用，文件转码使用
@SpringBootApplication
@MapperScan(basePackages = {"com.wangpan.mapper"})  //将项目中对应的mapper类的路径加进来就可以了
@EnableTransactionManagement
@EnableScheduling
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
