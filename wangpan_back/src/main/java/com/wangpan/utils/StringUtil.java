package com.wangpan.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/**
 * @author fangyixin
 * @date 2023/8/14 17:13
 */
public class StringUtil {
    /**
     * 生成随机数
     * @param count：随机数的个数
     * @return
     */
    public static final String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }

    public static final Boolean isEmpty(String s){
        if(s==null || s.equals("") || s.equals("null") || s.equals("\u0000")){
            return true;
        }else if(s.trim().equals("")){
            return true;
        }
        return false;
    }
}
