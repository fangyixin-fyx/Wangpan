package com.wangpan.exception;

import com.wangpan.controller.ABaseController;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.enums.ResponseCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.net.BindException;

@RestControllerAdvice
public class ExceptionHandlerBase extends ABaseController {
    private static final Logger logger= LoggerFactory.getLogger(ExceptionHandlerBase.class);

    @ExceptionHandler(value= Exception.class)
    Object handleException(Exception e, HttpServletRequest request){
        logger.error("请求错误，请求地址{}，错误信息：",request.getRequestURI(),e);
        ResponseVO response=new ResponseVO();
        //404
        if(e instanceof NoHandlerFoundException){
            response.setCode(ResponseCodeEnum.CODE_404.getCode());
            response.setInfo(ResponseCodeEnum.CODE_404.getMsg());
            response.setStatus(STATUE_ERROR);
        }else if(e instanceof BusinessException){
            //业务错误
            BusinessException businessException=(BusinessException) e;
            response.setCode(businessException.getCode());
            response.setInfo(businessException.getMessage());
            response.setStatus(STATUE_ERROR);
        }else if(e instanceof BindException){
            //参数类型错误
            response.setCode(ResponseCodeEnum.CODE_600.getCode());
            response.setInfo(ResponseCodeEnum.CODE_600.getMsg());
            response.setStatus(STATUE_ERROR);
        }else if(e instanceof BindException){
            //主键冲突错误
            response.setCode(ResponseCodeEnum.CODE_601.getCode());
            response.setInfo(ResponseCodeEnum.CODE_601.getMsg());
            response.setStatus(STATUE_ERROR);
        }else{
            response.setCode(ResponseCodeEnum.CODE_500.getCode());
            response.setInfo(ResponseCodeEnum.CODE_500.getMsg());
            response.setStatus(STATUE_ERROR);
        }
        return response;
    }

}
