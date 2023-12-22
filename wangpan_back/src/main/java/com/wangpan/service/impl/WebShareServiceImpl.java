package com.wangpan.service.impl;

import com.wangpan.config.BaseConfig;
import com.wangpan.config.RedisComponent;
import com.wangpan.constants.Constants;
import com.wangpan.dto.ShareSessionDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.dto.WebShareInfoDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.po.FileShare;
import com.wangpan.entity.po.User;
import com.wangpan.entity.query.SimplePage;
import com.wangpan.entity.vo.FileVO;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.enums.DateTimePatternEnum;
import com.wangpan.enums.FileFolderTypeEnum;
import com.wangpan.enums.PageSize;
import com.wangpan.enums.ResponseCodeEnum;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.FileMapper;
import com.wangpan.mapper.FileShareMapper;
import com.wangpan.mapper.UserMapper;
import com.wangpan.service.FileService;
import com.wangpan.service.WebShareService;
import com.wangpan.utils.CopyUtil;
import com.wangpan.utils.DateUtil;
import com.wangpan.utils.RedisUtils;
import com.wangpan.utils.StringTool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
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
    @Autowired
    private BaseConfig baseConfig;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private RedisUtils redisUtils;

    public WebShareInfoDto getShareInfoCommon(String shareId){
        WebShareInfoDto fileShare=fileShareMapper.getWebShareInfo(shareId);
        if(fileShare==null){
            return null;
        }
        Date curr=new Date();
        if(fileShare.getExpireTime()!=null && curr.after(fileShare.getExpireTime())){
            fileShare.setCode(ResponseCodeEnum.CODE_500.getCode());
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

    @Transactional(rollbackFor = Exception.class)
    public void save2MyAccount(String uid, String shareUserId, String shareFileIds, String myFolderId){
        if(uid.equals(shareUserId)){
            throw new BusinessException("自己分享的文件无法保存至自己的网盘");
        }
        String[] saveFids=shareFileIds.split(",");
        String redisSpaceKey=Constants.REDIS_KEY_USERSPACE_USED+uid;
        Date currTime=new Date();

        //遍历要转存的文件
        for(String fid:saveFids){
            //开始转存
            FileInfo fileInfo=fileMapper.selectByFid(fid);
            //判断空间是否充足
            //UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(uid);
            UserSpaceDto userSpaceDto=redisUtils.getUsedSpaceDto(uid);
            if(fileInfo.getFileSize()!=null&&fileInfo.getFileSize()+userSpaceDto.getUseSpace()>userSpaceDto.getTotalSpace()){
                throw new BusinessException("空间不足，无法转存");
            }

            String newFid=StringTool.getRandomNumber(Constants.LENGTH_10);
            //转存子文件
            if(fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
                saveSubFile(uid,shareUserId,fileInfo.getFid(),newFid);
            }
            //转存当前文件
            if(fileService.checkFileName(myFolderId,uid,fileInfo.getFileName(),fileInfo.getFolderType())>0){
                //重命名
                String fileName=fileInfo.getFileName()+"_"+
                        DateUtil.format(currTime,DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern());
                fileInfo.setFileName(fileName);
            }

            fileInfo.setFid(newFid);
            //fileInfo.setFilePid(Constants.ROOT_PID);
            fileInfo.setFilePid(myFolderId);
            fileInfo.setUserId(uid);
            fileInfo.setLastUpdateTime(currTime);
            fileInfo.setCreateTime(currTime);
            fileInfo.setRecoveryTime(null);

            //插入数据库
            int resInsert=fileMapper.insert(fileInfo);
            if(resInsert<1) throw new BusinessException("文件"+fileInfo.getFileName()+"转存失败");

            //转存非文件夹
            if(fileInfo.getFolderType().equals(FileFolderTypeEnum.FILE.getType())){
                //更新redis用户使用空间
                Long useSpace=userSpaceDto.getUseSpace()+fileInfo.getFileSize();
                userSpaceDto.setUseSpace(useSpace);
                //redisComponent.saveUserSpaceUsed(uid,userSpaceDto);
                redisUtils.setByTime(redisSpaceKey,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
            }

        }
        //更新用户数据库
        //UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(uid);
        UserSpaceDto userSpaceDto=redisUtils.getUsedSpaceDto(uid);
        int result=userMapper.updateUserSpace2(uid,userSpaceDto.getUseSpace(),null);
        if(result<1) throw new BusinessException("更新user表使用空间失败");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveSubFile(String uid,String ownerId,String folderId,String pid){
        //获取所有子文件
        List<FileInfo> subList=fileMapper.selectFileByUidAndPid(ownerId,folderId);
        Date currTime=new Date();
        String redisSpaceKey=Constants.REDIS_KEY_USERSPACE_USED+uid;
        for(FileInfo subFile:subList){
            //判断用户空间是否充足
            Long fileSize=subFile.getFileSize();
            //UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(uid);
            UserSpaceDto userSpaceDto=redisUtils.getUsedSpaceDto(uid);
            if(fileSize!=null&&fileSize+userSpaceDto.getUseSpace()>userSpaceDto.getTotalSpace()){
                throw new BusinessException("空间不足，无法转存");
            }
            //转存子文件
            String newFid=StringTool.getRandomNumber(Constants.LENGTH_10);
            if(subFile.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
                saveSubFile(uid,ownerId,subFile.getFid(),newFid);
            }

            if(fileService.checkFileName(folderId,uid,subFile.getFileName(),subFile.getFolderType())>0){
                //重命名
                String fileName=subFile.getFileName()+"_"+
                        DateUtil.format(currTime,DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern());
                subFile.setFileName(fileName);
            }

            subFile.setFid(newFid);
            subFile.setFilePid(pid);
            subFile.setUserId(uid);
            subFile.setLastUpdateTime(currTime);
            subFile.setCreateTime(currTime);
            subFile.setRecoveryTime(null);

            //插入数据库
            int resInsert=fileMapper.insert(subFile);
            if(resInsert<1) throw new BusinessException("文件"+subFile.getFileName()+"转存失败");

            //转存文件
            if(subFile.getFolderType().equals(FileFolderTypeEnum.FILE.getType())){
                //更新redis用户使用空间
                userSpaceDto.setUseSpace(userSpaceDto.getUseSpace()+subFile.getFileSize());
                //redisComponent.saveUserSpaceUsed(uid,userSpaceDto);
                redisUtils.setByTime(redisSpaceKey,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
            }
        }

    }
}
