package com.wangpan.controller;

import com.wangpan.annotations.GlobalInterceptor;
import com.wangpan.config.RedisComponent;
import com.wangpan.constants.Constants;
import com.wangpan.dto.SysSettingsDto;
import com.wangpan.dto.UserDto;
import com.wangpan.dto.UserForAdminDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.po.User;
import com.wangpan.entity.query.FileQuery;
import com.wangpan.entity.query.UserQuery;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.enums.UserStateEnum;
import com.wangpan.service.FileService;
import com.wangpan.service.FileShareService;
import com.wangpan.service.UserService;
import com.wangpan.utils.StringTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

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
    @Autowired
    private SysSettingsDto sysSettingsDto;

    @PostMapping("/getSysSettings")
    public ResponseVO getSystemSettings(){
        //return getSuccessResponseVO(redisComponent.getSysSettingDto());
        return getSuccessResponseVO(sysSettingsDto);
    }

    @PostMapping("/saveSysSettings")
    public ResponseVO saveSysSettings(String registerEmailTitle,String registerEmailContent,String userInitUseSpace){
        /*
        SysSettingsDto sysSettingsDto=new SysSettingsDto();
        sysSettingsDto.setRegisterEmailContent(registerEmailContent);
        sysSettingsDto.setRegisterEmailTitle(registerEmailTitle);
        sysSettingsDto.setUserInitUseSpace(Integer.valueOf(userInitUseSpace));
        if(redisComponent.setSysSettingDto(sysSettingsDto)){
            return getSuccessResponseVO(null);
        }else{
            return getFailResponseVO("保存失败");
        }

         */
        sysSettingsDto.setRegisterEmailContent(registerEmailContent);
        sysSettingsDto.setRegisterEmailTitle(registerEmailTitle);
        sysSettingsDto.setUserInitUseSpace(Integer.valueOf(userInitUseSpace));
        return getSuccessResponseVO(null);
    }

    @PostMapping("/loadUserList")
    public ResponseVO getUserList(UserQuery userQuery){
        PaginationResultVO<UserForAdminDto> resultVO=userService.adminGetUserListByPage(userQuery);
        return getSuccessResponseVO(resultVO);
    }

    @PostMapping("/updateUserStatus")
    public ResponseVO updateUserStatus(String userId, String status,HttpSession session){
        String currUid=getUserInfoFromSession(session).getUid();
        int result=userService.updateStatusByAdmin(userId,status,currUid);
        if(result==-1){
            //退出登录
            //session.invalidate();
        }
        else if(result<1){
            return getFailResponseVO("禁用用户失败");
        }

        return getSuccessResponseVO(null);
    }

    @PostMapping("/updateUserSpace")
    public ResponseVO updateUserSpace(HttpSession session,String userId,String changeSpace){
        int result=userService.updateUserSpace(userId,changeSpace,getUserInfoFromSession(session).getUid());
        if(result>0) return getSuccessResponseVO(null);
        else return getFailResponseVO("修改用户空间失败");
    }

    @PostMapping("/loadFileList")
    private ResponseVO loadFileList(String pageNo,String pageSize,String fileNameFuzzy){
        FileQuery fileQuery=new FileQuery();
        if(!StringTool.isEmpty(pageNo)){
            fileQuery.setPageNo(Integer.valueOf(pageNo));
        }
        if(!StringTool.isEmpty(pageSize)){
            fileQuery.setPageSize(Integer.valueOf(pageSize));
        }
        fileQuery.setFileNameFuzzy(fileNameFuzzy);
        fileQuery.setOrderBy("lastUpdateTime desc");
        PaginationResultVO<FileInfo> result=fileService.adminFindListByPage(fileQuery);
        return getSuccessResponseVO(result);
    }

}
