package com.wangpan.controller;

import com.wangpan.annotations.GlobalInterceptor;
import com.wangpan.config.RedisComponent;
import com.wangpan.dto.SysSettingsDto;
import com.wangpan.dto.UserDto;
import com.wangpan.dto.UserForAdminDto;
import com.wangpan.entity.po.User;
import com.wangpan.entity.query.UserQuery;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.service.FileService;
import com.wangpan.service.FileShareService;
import com.wangpan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fangyixin
 * @date 2023/12/7 12:59
 */
@RestController
@RequestMapping("/admin")
public class AdminController extends ABaseController{
    @Autowired
    private FileService fileService;
    @Autowired
    private FileShareService fileShareService;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserService userService;

    @PostMapping("/getSysSettings")
    public ResponseVO getSystemSettings(){
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }

    @PostMapping("/saveSysSettings")
    public ResponseVO saveSysSettings(String registerEmailTitle,String registerEmailContent,String userInitUseSpace){
        SysSettingsDto sysSettingsDto=new SysSettingsDto();
        sysSettingsDto.setRegisterEmailContent(registerEmailContent);
        sysSettingsDto.setRegisterEmailTitle(registerEmailTitle);
        sysSettingsDto.setUserInitUseSpace(Integer.valueOf(userInitUseSpace));
        if(redisComponent.setSysSettingDto(sysSettingsDto)){
            return getSuccessResponseVO(null);
        }else{
            return getFailResponseVO("保存失败");
        }

    }

    @PostMapping("/loadUserList")
    public ResponseVO getUserList(UserQuery userQuery){
        PaginationResultVO<UserForAdminDto> resultVO=userService.adminGetUserListByPage(userQuery);
        return getSuccessResponseVO(resultVO);
    }

}
