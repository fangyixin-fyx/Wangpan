package com.wangpan.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 把一个对象copy为另一个对象
 * @author fangyixin
 * @date 2023/11/27 15:04
 */
public class CopyUtil {
    public static <T,S> List<T> copyList(List<S> sList,Class<T> clazz){
        List<T> list=new ArrayList<>();
        for(S s:sList){
            T t=null;
            try {
                t=clazz.newInstance();
            }catch (Exception e){
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s,t);
            list.add(t);
        }
        return list;
    }
    public static <T,S> T copy(S s,Class<T> clazz){
        T t=null;
        try {
            t=clazz.newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        BeanUtils.copyProperties(s,t);
        return t;
    }
}
