package com.wangpan.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则校验工具类
 * @author fangyixin
 * @date 2023/11/16 16:03
 */
public class VerifyUtils {

    public static boolean verify(String regex, String value){
        if(StringUtil.isEmpty(value)) return false;
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(value);
        return matcher.matches();
    }

    /*
    public static boolean verify(VerifyRegexEnum regex, String value){
        return verify(regex.getRegex(),value);
    }

     */

}
