package com.wangpan.controller;

import com.wangpan.annotations.GlobalInterceptor;
import com.wangpan.annotations.VerifyParam;
import com.wangpan.config.BaseConfig;
import com.wangpan.constants.Constants;
import com.wangpan.dto.UserDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.entity.po.User;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.enums.VerifyRegexEnum;
import com.wangpan.exception.BusinessException;
import com.wangpan.service.EmailCodeService;
import com.wangpan.service.UserService;
import com.wangpan.utils.RedisUtils;
import com.wangpan.utils.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

/**
 * @author fangyixin
 * @date 2023/8/10 16:55
 */
@RestController
public class UserController extends ABaseController {
    private static final Logger logger= LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private EmailCodeService emailCodeService;
    @Autowired
    private BaseConfig baseConfig;
    @Resource
    private RedisUtils redisUtils;

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
    @GlobalInterceptor(checkParam = true,checkLogin = false)
    public ResponseVO sendEmailCode(HttpSession session,
                                    @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL) String email,
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

    @PostMapping("/register")
    @GlobalInterceptor(checkParam = true,checkLogin = false)
    public ResponseVO register(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL) String email,
                               @VerifyParam(required = true) String nickName,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min=8, max=18) String password,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode){
        try{
            if(!checkCode.equals(session.getAttribute(Constants.CHECK_CODE_KEY))){ //如果不匹配，抛异常
                throw new BusinessException("验证码不正确");
            }
            userService.register(nickName,password,email,emailCode);  //username用不了因为前端定死
            return getSuccessResponseVO(null);
        } finally {
            //用完后重置验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    @PostMapping("/login")
    @GlobalInterceptor(checkParam = true,checkLogin = false)
    public ResponseVO login(HttpSession session,
                            @VerifyParam(required = true) String email,
                            @VerifyParam(required = true) String password,
                            @VerifyParam(required = true) String checkCode)
    {
        try {
            if(!checkCode.equals(session.getAttribute(Constants.CHECK_CODE_KEY))){
                throw new BusinessException("验证码不正确");
            }
            UserDto sessionWebUserDto =userService.login(email,password);
            session.setAttribute(Constants.SESSION_USER, sessionWebUserDto); //存入session
            return getSuccessResponseVO(sessionWebUserDto);   //返给前端
        }finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    //登陆界面忘记密码
    @PostMapping("/resetPwd")
    @GlobalInterceptor(checkParam = true,checkLogin = false)
    public ResponseVO resetPwd(HttpSession session,
                            @VerifyParam(required = true) String email,
                            @VerifyParam(required = true) String password,
                            @VerifyParam(required = true) String checkCode,
                            @VerifyParam(required = true) String emailCode   )
    {
        try{
            if(!checkCode.equals(session.getAttribute(Constants.CHECK_CODE_KEY))){
                throw new BusinessException("验证码不正确");
            }
            userService.resetPwd(email,password,emailCode);
            return getSuccessResponseVO(null);   //返给前端
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    @GetMapping("/getAvatar/{userId}")
    public void getAvatar(HttpServletResponse response, @PathVariable("userId") String userId){
        /*
        String path=baseConfig.getProjectFolder()+Constants.AVATAR_PATH;
        File pathFile=new File(path);
        //判断路径是否存在
        if(!pathFile.exists()) pathFile.mkdirs();
        String avatarFilePath=path+userId+".jpg"; //绝对路径
        File avatarFile=new File(avatarFilePath);
        //如果头像不存在，赋予默认头像
        if(!avatarFile.exists()){
            String defaultAvatarFilePath=path+Constants.AVATAR_DEFAULT;
            //如果默认头像不存在
            if(!new File(defaultAvatarFilePath).exists()) noDefaultAvatar(response);  //这里可改进
            avatarFilePath=defaultAvatarFilePath;
        }
         */
        String avatarFilePath=userService.getAvatar(userId);
        response.setContentType("image/jpg");
        readFile(response,avatarFilePath);
    }


    //获取用户信息---未用到
    @GetMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO getUserInfo(HttpSession session){
        UserDto sessionWebUserDto =getUserInfoFromSession(session);
        return getSuccessResponseVO(sessionWebUserDto);
    }

    //获取用户空间
    //@PostMapping(value = "/getUseSpace",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequestMapping("/getUseSpace")
    @GlobalInterceptor
    public ResponseVO getUseSpace(HttpSession session){
        UserDto userDto =getUserInfoFromSession(session);
        //UserSpaceDto spaceDto= redisComponent.getUsedSpaceDto(userDto.getUid());
        UserSpaceDto spaceDto=(UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USERSPACE_USED+userDto.getUid());
        return getSuccessResponseVO(spaceDto);
    }

    //退出登录
    @RequestMapping("/logout")  //Post法
    public ResponseVO logout(HttpSession session){
        userService.logout(session);
        return getSuccessResponseVO(null);
    }

    //更新头像
    @PostMapping("/updateUserAvatar")
    @GlobalInterceptor
    public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar){
        UserDto userDto =getUserInfoFromSession(session);
        String baseFilePath=baseConfig.getProjectFolder()+Constants.AVATAR_PATH;
        File targetFileFolder=new File(baseFilePath);
        if(!targetFileFolder.exists()) targetFileFolder.mkdirs();
        File avatarFile=new File(baseFilePath+"/"+ userDto.getUid()+".jpg");
        try{
            avatar.transferTo(avatarFile);
        }catch (Exception e){
            logger.error("头像上传失败！请重新尝试。",e);
        }

        User user=new User();
        //如果是qq登录则有默认qq头像，当更新头像后使用uid标记的上传头像，因此qq头像置空，没有自己上传的头像才使用qq头像
        //即优先级 自己上传的头像>qq头像
        //但是getAvatar那里没有判断是否qq登录是否上传了本地头像，有改进之处
        user.setQqIcon("");
        userService.updateUserByUid(user, userDto.getUid());
        userDto.setAvatar(null);
        session.setAttribute(Constants.SESSION_USER, userDto);
        return getSuccessResponseVO(null);
    }

    //登陆后更新密码
    @PostMapping("/updatePassword")
    @GlobalInterceptor
    public ResponseVO updatePassword(HttpSession session,String password){
        UserDto userDto =getUserInfoFromSession(session);
        User user=new User();
        user.setPassword(StringTool.encodeByMD5(password));
        userService.updateUserByUid(user, userDto.getUid());
        return getSuccessResponseVO(null);
    }

}
