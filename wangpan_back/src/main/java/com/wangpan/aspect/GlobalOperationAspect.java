package com.wangpan.aspect;

import com.wangpan.annotations.GlobalInterceptor;
import com.wangpan.annotations.VerifyParam;
import com.wangpan.enums.ResponseCodeEnum;
import com.wangpan.exception.BusinessException;
import com.wangpan.utils.StringUtil;
import com.wangpan.utils.VerifyUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;


/**
 * @author fangyixin
 * @date 2023/11/16 13:56
 */
@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {

    @Autowired
    private static final Logger logger= LoggerFactory.getLogger(GlobalInterceptor.class);

    private static final String TYPE_STRING="java.lang.String";
    private static final String TYPE_LONG="java.lang.Long";
    private static final String TYPE_INTEGER="java.lang.Integer";

    //定义切点: 被@GlobalInterceptor注释的方法
    @Pointcut("@annotation(com.wangpan.annotations.GlobalInterceptor)")
    private void requestInterceptor(){

    }

    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint joinPoint){
        try {
            Object target=joinPoint.getTarget(); //获得被代理的对象
            Object[] arguments=joinPoint.getArgs(); //获取传入目标方法的参数对象
            String methodName=joinPoint.getSignature().getName(); //获得目标方法名
            Class<?>[] parameterType=((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes(); //获得参数类型标准
            Method method=target.getClass().getMethod(methodName,parameterType); //通过反射获取方法对象
            GlobalInterceptor interceptor=method.getAnnotation(GlobalInterceptor.class);    //获取方法的指定注解对象，无该注解则返回null
            if(interceptor==null){
                return;
            }
            // 校验登录
            /*
            if(interceptor.checkLogin()||interceptor.checkAdmin()){
                checkLogin(interceptor.checkAdmin());
            }
             */
            //校验参数
            if(interceptor.checkParam()){
                validateParams(method,arguments);
            }

        }catch (BusinessException e){
            logger.error("全局拦截器异常",e);
            throw e;
        }catch (Exception e){
            logger.error("全局拦截器异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }catch (Throwable e){
            logger.error("全局拦截器异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    /**
     *
     * @param method：标准方法对象
     * @param arguments:需检验的方法参数
     */
    private void validateParams(Method method, Object[] arguments) {
        Parameter[] parameters= method.getParameters();
        for(int i=0;i< parameters.length;i++){
            VerifyParam verifyParam=parameters[i].getAnnotation(VerifyParam.class);
            if(verifyParam==null) continue;
            //如果是基本参数类型
            if(TYPE_STRING.equals(parameters[i].getParameterizedType().getTypeName())
                    || TYPE_LONG.equals(parameters[i].getParameterizedType().getTypeName())){
                checkValue(arguments[i],verifyParam);
            }
            //如果是对象
            else{
                checkObjectValue(parameters[i],arguments[i]);
            }
        }
    }

    /**
     *
     * @param value：传入的待校验的值
     * @param verifyParam：传入参数的校验注解信息
     */
    private void checkValue(Object value, VerifyParam verifyParam){
        Integer length=(value==null ? 0 : value.toString().length() );
        Boolean isEmpty=(value==null || StringUtil.isEmpty(value.toString()));
        //校验空
        if(isEmpty && verifyParam.required()) throw new BusinessException(ResponseCodeEnum.CODE_600);
        //校验长度
        if(!isEmpty &&
                ((verifyParam.max()!=-1&&verifyParam.max()<length) || (verifyParam.min()!=-1&&length< verifyParam.min())))
        {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //校验正则
        if(!isEmpty && !StringUtil.isEmpty(verifyParam.regex().getRegex())
                && !VerifyUtils.verify(verifyParam.regex().getRegex(), String.valueOf(value))){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    /**
     *
     * @param parameter：标准方法参数
     * @param value：需检验的对象参数
     */
    private void checkObjectValue(Parameter parameter, Object value){
        try{
            String typeName=parameter.getParameterizedType().getTypeName();
            //根据反射获取class类对象
            Class clazz=Class.forName(typeName);
            //获取标准属性信息
            Field[] fields=clazz.getDeclaredFields();
            for(Field field:fields){
                VerifyParam fieldVerify=field.getAnnotation(VerifyParam.class);
                if(fieldVerify==null) continue;
                field.setAccessible(true);
                //返回待检验对象value上此属性字段的值
                Object resultVaule=field.get(value);
                checkValue(resultVaule,fieldVerify);
            }
        }catch (BusinessException e){
            logger.error("校验对象参数失败",e);
            throw e;
        }catch (Exception e){
            logger.error("校验对象参数失败",e);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }
}
