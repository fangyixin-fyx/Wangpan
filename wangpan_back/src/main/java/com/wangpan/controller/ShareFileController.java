package com.wangpan.controller;

import com.wangpan.entity.po.FileShare;
import com.wangpan.entity.query.FileShareQuery;
import com.wangpan.entity.vo.FileVO;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.service.FileShareService;
import com.wangpan.utils.StringTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author fangyixin
 * @date 2023/12/5 16:08
 */
@RestController
@RequestMapping("/share")
public class ShareFileController extends ABaseController{
    @Autowired
    private FileShareService fileShareService;

    @PostMapping("loadShareList")
    public ResponseVO loadFileList(HttpSession session){
        FileShareQuery fileShareQuery=new FileShareQuery();
        fileShareQuery.setUserId(getUserInfoFromSession(session).getUid());
        fileShareQuery.setOrderBy("shareTime desc");
        PaginationResultVO<FileShare> result=fileShareService.findListByPage(fileShareQuery);
        return getSuccessResponseVO(result);
    }

    @PostMapping("/shareFile")
    public ResponseVO shareFile(HttpSession session,@RequestParam("fileId") String fid,
                                @RequestParam("validType") Integer validType,
                                @RequestParam(name = "code",required = false) String code){
        FileShareQuery fileShareQuery=new FileShareQuery();
        fileShareQuery.setFileId(fid);
        fileShareQuery.setUserId(getUserInfoFromSession(session).getUid());
        fileShareQuery.setValidType(validType);
        if(StringTool.isEmpty(code)){
            fileShareQuery.setCodeType(1);
        }else{
            fileShareQuery.setCodeType(0);
            fileShareQuery.setCode(code);
        }
        FileShare fileShare=fileShareService.shareFile(fileShareQuery);
        return getSuccessResponseVO(fileShare);
    }

    @PostMapping("/cancelShare")
    public ResponseVO cancelShare(@RequestParam("shareIds") String sids){
        fileShareService.deleteFileShareBatch(sids);
        return getSuccessResponseVO(null);
    }

}
