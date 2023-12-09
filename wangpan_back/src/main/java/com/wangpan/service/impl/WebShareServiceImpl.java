package com.wangpan.service.impl;

import com.wangpan.constants.Constants;
import com.wangpan.dto.ShareSessionDto;
import com.wangpan.dto.WebShareInfoDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.po.FileShare;
import com.wangpan.entity.query.FileQuery;
import com.wangpan.entity.query.SimplePage;
import com.wangpan.entity.vo.FileVO;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.enums.PageSize;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.FileMapper;
import com.wangpan.mapper.FileShareMapper;
import com.wangpan.service.FileService;
import com.wangpan.service.WebShareService;
import com.wangpan.utils.CopyUtil;
import com.wangpan.utils.StringTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author fangyixin
 * @date 2023/12/7 20:43
 */
@Service("webShareService")
public class WebShareServiceImpl implements WebShareService {
    @Autowired
    private FileShareMapper fileShareMapper;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FileService fileService;

    public WebShareInfoDto getShareInfoCommon(String shareId){
        WebShareInfoDto fileShare=fileShareMapper.getWebShareInfo(shareId);
        if(fileShare==null){
            throw new BusinessException("分享文件已不存在");
        }
        Date curr=new Date();
        if(fileShare.getExpireTime()!=null && curr.after(fileShare.getExpireTime())){
            throw new BusinessException("分享文件已经失效");
        }
        return fileShare;
    }

    @Override
    public ShareSessionDto checkCode(String shareId,String code){
        FileShare fileShare=fileShareMapper.selectByShareId(shareId);
        Date curr=new Date();
        if(fileShare==null || curr.after(fileShare.getExpireTime())){
            throw new BusinessException("分享文件不存在或已失效");
        }
        if(!code.equals(fileShare.getCode())){
            throw new BusinessException("验证码错误，请重新输入");
        }
        //更新查看次数
        fileShareMapper.updateShowCountByShareId(shareId);
        //返回session数据
        ShareSessionDto shareSessionDto=new ShareSessionDto();
        shareSessionDto.setShareId(shareId);
        shareSessionDto.setShareUserId(fileShare.getUserId());
        shareSessionDto.setFileId(fileShare.getFileId());
        shareSessionDto.setExpireTime(fileShare.getExpireTime());
        return shareSessionDto;
    }

    public PaginationResultVO<FileInfo> getList(String shareId, String filePid){
        List<FileInfo> list=null;
        //检查pid是否是根文件，还是分享文件夹内的文件
        if(!StringTool.isEmpty(filePid) && !filePid.equals(Constants.ROOT_PID)){
            //此时filePid为父文件id,获取子文件
            list=fileMapper.selectShareSubFile(filePid);
        }else{
            list=fileMapper.selectFileList(shareId);
        }
        List<FileVO> resultList= CopyUtil.copyList(list,FileVO.class);
        SimplePage page=new SimplePage(0, list.size(), PageSize.SIZE15.getSize());
        PaginationResultVO<FileInfo> result=new PaginationResultVO(list.size(), page.getPageSize(), page.getPageNo(),page.getPageTotal(),resultList);
        return result;
    }


    public List<FileVO> getFolderInfo(String path, String shareUserId){
        List<FileInfo> list=fileService.getFolderInfo(path,shareUserId);
        List<FileVO> result=CopyUtil.copyList(list,FileVO.class);
        return result;
    }
}
