package com.wangpan.controller;

import com.wangpan.annotations.GlobalInterceptor;
import com.wangpan.annotations.VerifyParam;
import com.wangpan.constants.Constants;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.exception.BusinessException;
import com.wangpan.service.EmailCodeService;
import com.wangpan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author fangyixin
 * @date 2023/8/10 16:55
 */
@RestController
public class UserController extends ABaseController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailCodeService emailCodeService;

    //登录验证码
    @RequestMapping("/checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
        CreateImageCode vCode = new CreateImageCode(130,38,5,10);
        response.setHeader("Pragma","no-cache");
        response.setHeader("Cache-Control","no-cache");
        response.setDateHeader("Expires",0);
        response.setContentType("image/jpeg");
        String code=vCode.getCode();
        //0：注册登录验证码(默认) 1：邮箱验证码
        if(type==null || type==0){
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        }else{
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        vCode.write(response.getOutputStream());
    }


    @PostMapping("/sendEmailCode")
    @GlobalInterceptor(checkParam = true)
    public ResponseVO sendEmailCode(HttpSession session, @VerifyParam(required = true) String email,
                                    @VerifyParam(required = true) String checkCode,
                                    @VerifyParam(required = true) Integer type){
        try{
            if(!checkCode.equals(session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))){ //如果不匹配，抛异常
                throw new BusinessException("图片验证码不正确");
            }
            emailCodeService.sendEmailCode(email,type);
            return getSuccessResponseVO(null);
        } finally {
            //用完后重置验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }






}
