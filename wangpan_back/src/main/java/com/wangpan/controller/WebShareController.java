package com.wangpan.controller;

import com.wangpan.constants.Constants;
import com.wangpan.dto.ShareSessionDto;
import com.wangpan.dto.UserDto;
import com.wangpan.dto.WebShareInfoDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.query.FileQuery;
import com.wangpan.entity.vo.FileVO;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.enums.ResponseCodeEnum;
import com.wangpan.enums.UploadStatusEnum;
import com.wangpan.exception.BusinessException;
import com.wangpan.service.FileService;
import com.wangpan.service.FileShareService;
import com.wangpan.service.UserService;
import com.wangpan.service.WebShareService;
import com.wangpan.utils.StringTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 外部分享
 * @author fangyixin
 * @date 2023/12/7 20:25
 */
@RestController
@RequestMapping("/showShare")
public class WebShareController extends ABaseController{
    @Autowired
    private WebShareService webShareService;
    @Autowired
    private FileService fileService;

    @PostMapping("/getShareInfo")
    public ResponseVO getShareInfo(String shareId){
        WebShareInfoDto webShareInfoDto=webShareService.getShareInfoCommon(shareId);
        if(webShareInfoDto==null || webShareInfoDto.getCode()==ResponseCodeEnum.CODE_500.getCode()){
            return getFailResponseVOWithMsg("文件分享已取消或已失效");
        }
        return getSuccessResponseVO(webShareInfoDto);
    }

    @PostMapping("/getShareLoginInfo")
    public ResponseVO getShareLoginInfo(HttpSession session,String shareId){
        ShareSessionDto shareSessionDto=(ShareSessionDto) session.getAttribute(Constants.SESSION_SHARE+shareId);
        if(shareSessionDto==null){
            //前端接收后会让用户输入校验码,跳到getShareInfo()
            return getSuccessResponseVO(null);
        }

        WebShareInfoDto webShareInfoDto=webShareService.getShareInfoCommon(shareId);
        //判断是否是登录用户自己分享的文件,前端设置了两个按钮:取消分享和保存到网盘
        UserDto userDto=getUserInfoFromSession(session);
        if(userDto!=null && userDto.getUid().equals(webShareInfoDto.getUserId())){
            webShareInfoDto.setCurrentUser(true);
        }else{
            webShareInfoDto.setCurrentUser(false);
        }
        //验证通过即可获得分享文件数据
        return getSuccessResponseVO(webShareInfoDto);
    }

    @PostMapping("/checkShareCode")
    public ResponseVO checkShareCode(HttpSession session,String shareId,String code){
        ShareSessionDto shareSessionDto=webShareService.checkCode(shareId,code);
        session.setAttribute(Constants.SESSION_SHARE+shareId,shareSessionDto);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/loadFileList")
    public ResponseVO loadShareFileList(HttpSession session,String shareId,String filePid){
        ShareSessionDto shareSessionDto=(ShareSessionDto) session.getAttribute(Constants.SESSION_SHARE+shareId);
        if(shareSessionDto==null){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        Date curr=new Date();
        if(shareSessionDto.getExpireTime()!=null&&curr.after(shareSessionDto.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }

        PaginationResultVO<FileInfo> resultVO=webShareService.getList(shareId,filePid);
        return getSuccessResponseVO(resultVO);
    }

    @PostMapping("/getFolderInfo")
    public ResponseVO getFolderInfo(HttpSession session,String shareId,String path){
        ShareSessionDto shareSessionDto=(ShareSessionDto) session.getAttribute(Constants.SESSION_SHARE+shareId);
        List<FileVO> list= webShareService.getFolderInfo(path,shareSessionDto.getShareUserId());
        return getSuccessResponseVO(list);
    }

    @PostMapping("/getFile/{shareId}/{fileId}")
    public ResponseVO getFile(HttpSession session, HttpServletResponse response,
                              @PathVariable("shareId") String shareId,
                              @PathVariable("fileId") String fileId){
        ShareSessionDto shareSessionDto=(ShareSessionDto) session.getAttribute(Constants.SESSION_SHARE+shareId);
        String filePath=fileService.getFile(fileId,shareSessionDto.getShareUserId());
        readFile(response,filePath);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/createDownloadUrl/{shareId}/{fileId}")
    public ResponseVO createDownloadUrl(HttpSession session,
                                        @PathVariable("shareId") String shareId,@PathVariable("fileId") String fileId){
        ShareSessionDto shareSessionDto=(ShareSessionDto) session.getAttribute(Constants.SESSION_SHARE+shareId);
        String code=fileService.createDownloadUrl(fileId,shareSessionDto.getShareUserId());
        return getSuccessResponseVO(code);
    }

    @GetMapping("/download/{code}")
    public void downloadFile(@PathVariable("code") String code, HttpServletRequest request,
                                   HttpServletResponse response) throws UnsupportedEncodingException {
        Map<String,String> map=fileService.download(code);
        String path=map.get("filePath");
        String fileName=map.get("fileName");
        response.setContentType("application/x-msdownload; charset=UTF-8");
        //如果是IE浏览器
        if(request.getHeader("User-Agent").toLowerCase().indexOf("msie")>0){
            fileName= URLEncoder.encode(fileName,"UTF-8");
        }else{
            fileName=new String(fileName.getBytes("UTF-8"),"ISO8859-1");
        }
        response.setHeader("Content-Disposition","attachment;filename=\""+fileName+"\"");
        //读取文件
        readFile(response,path);
    }

    @PostMapping("/saveShare")
    public ResponseVO saveShare2MyAccount(HttpSession session,String shareId,String shareFileIds,String myFolderId){
        ShareSessionDto shareSessionDto=(ShareSessionDto) session.getAttribute(Constants.SESSION_SHARE+shareId);
        UserDto userDto=getUserInfoFromSession(session);
        webShareService.save2MyAccount(userDto.getUid(),shareSessionDto.getShareUserId(),shareFileIds,myFolderId);
        return getSuccessResponseVO(UploadStatusEnum.UPLOADED);
    }

}
