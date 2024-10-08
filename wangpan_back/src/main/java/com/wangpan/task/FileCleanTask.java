package com.wangpan.task;

import com.wangpan.constants.Constants;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.po.FileShare;
import com.wangpan.enums.FileFolderTypeEnum;
import com.wangpan.mapper.FileMapper;
import com.wangpan.mapper.FileShareMapper;
import com.wangpan.mapper.UserMapper;
import com.wangpan.service.FileService;
import com.wangpan.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author fangyixin
 * @date 2023/12/9 21:21
 */
@Component("fileCleanTask")
public class FileCleanTask {
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FileShareMapper fileShareMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private RedisUtils redisUtils;


    @Scheduled(fixedDelay = 60*60*1000)
    public void execute(){
        //删除回收站过期文件
        cleanFile();
        //删除过期分享文件
        cleanShareFile();
    }
    @Transactional
    public void cleanFile(){
        Date currTime=new Date();
        List<FileInfo> list=fileMapper.selectExpiredFile(currTime);
        Set<String> uidSet=new HashSet<>();
        //获取文件夹的子文件信息
        List<String> delIds=new ArrayList<>();
        for(FileInfo fileInfo:list){
            if(fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
                //获取子文件的所有ID
                fileService.deleteSubFileCompletely(fileInfo.getFid(),fileInfo.getUserId(),delIds);
            }
            uidSet.add(fileInfo.getUserId());
            //删除父文件
            fileMapper.cleanByFid(fileInfo.getFid());
        }
        //数据库操作
        //删除子文件
        if(delIds.size()>0){
            String[] subIds=new String[delIds.size()];
            fileMapper.deleteCompletelyByFid(delIds.toArray(subIds));
        }
        //更新用户空间
        for(String uid:uidSet){
            Long currSize=fileMapper.getUsedSpaceByUid(uid);
            userMapper.updateUserSpace2(uid,currSize,null);
            //如果是当前登录用户，则需要更新redis
            //UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(uid);
            UserSpaceDto userSpaceDto=redisUtils.getUsedSpaceDto(uid);
            if(userSpaceDto!=null){
                userSpaceDto.setUseSpace(currSize);
                redisUtils.setByTime(Constants.REDIS_KEY_USERSPACE_USED+uid,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
            }
        }
    }
    @Transactional
    public void cleanShareFile(){
        Date currTime=new Date();
        List<FileShare> list=fileShareMapper.selectExpiredFile(currTime);
        for(FileShare s:list){
            fileShareMapper.cleanByShareId(s.getShareId());
        }
    }
}
