package com.wangpan.controller;

import com.wangpan.annotations.GlobalInterceptor;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.query.FileQuery;
import com.wangpan.entity.vo.FileVO;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.enums.FileCategoryEnum;
import com.wangpan.enums.FileDelFlagEnum;
import com.wangpan.service.FileService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Arrays;

/**
 * 回收站
 * @author fangyixin
 * @date 2023/12/4 20:11
 */
@RestController
@RequestMapping("/recycle")
public class RecycleBinController extends ABaseController{
    @Autowired
    private FileService fileService;


    @PostMapping("/loadRecycleList")
    @GlobalInterceptor
    public ResponseVO loadRecycle(HttpSession session, Integer pageNo,Integer pageSize) {
        FileQuery fileQuery=new FileQuery();
        fileQuery.setPageNo(pageNo);
        fileQuery.setPageSize(pageSize);
        fileQuery.setUserId(getUserInfoFromSession(session).getUid());
        fileQuery.setOrderBy("recoveryTime desc");
        fileQuery.setDelFlag(FileDelFlagEnum.RECYCLE.getStatus());
        PaginationResultVO<FileInfo> resultVO=fileService.findListByPage(fileQuery);
        //将FileInfo转为FileInfoVO
        return getSuccessResponseVO(convert2PaginationVO(resultVO, FileVO.class));
    }

    @PostMapping("/recoverFile")
    public ResponseVO recoveryFile(@RequestParam("fileIds") String fileIds){
        String[] fileArray=fileIds.split(",");
        FileQuery fileQuery=new FileQuery();
        fileQuery.setFileIdArray(fileArray);
        fileQuery.setDelFlag(FileDelFlagEnum.RECYCLE.getStatus());
        fileService.recoveryFile(fileQuery);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/delFile")
    public ResponseVO delFiles(@RequestParam("fileIds") String fids,HttpSession session){
        fileService.deleteFileCompletely(fids,getUserInfoFromSession(session).getUid());
        return getSuccessResponseVO(null);
    }

}
