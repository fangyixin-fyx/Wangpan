package com.wangpan.task;

import com.wangpan.config.BaseConfig;
import com.wangpan.constants.Constants;
import com.wangpan.dto.UserDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.enums.DateTimePatternEnum;
import com.wangpan.enums.FileStatusEnum;
import com.wangpan.enums.FileTypeEnum;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.FileMapper;
import com.wangpan.service.impl.FileServiceImpl;
import com.wangpan.utils.DateUtil;
import com.wangpan.utils.FfmpegUtils;
import com.wangpan.utils.RedisUtils;
import com.wangpan.utils.ScaleFilter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * 异步任务执行类
 * @author fangyixin
 * @date 2024/1/6 15:37
 */
@Component
public class AsyncTask {
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private BaseConfig baseConfig;
    @Autowired
    private RedisUtils redisUtils;

    private static final Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 文件转码，异步合并分片
     * @param fileId
     * @param userDto
     */
    @Async
    public void transferFile(String fileId, UserDto userDto){
        Boolean transferSuccess=true;
        String targetFilePath=null;
        String cover=null;
        FileTypeEnum fileTypeEnum=null;
        FileInfo fileInfo=(FileInfo) fileMapper.selectByFidAndUserId(fileId,userDto.getUid());
        if(fileInfo==null || !fileInfo.getStatus().equals(FileStatusEnum.TRANSFER.getStatus())){
            return;
        }
        try{
            logger.info("-----开始异步合并文件-----");
            //找到临时目录
            String tempFileName=baseConfig.getProjectFolder()+"/"+ Constants.FOLD_TEMP;
            //String currentUserFolderName=userDto.getUid()+fileId;
            String md5=fileInfo.getFileMd5();
            String currentUserFolderName=userDto.getUid()+md5;

            File fileFolder=new File(tempFileName+currentUserFolderName);

            String fName=fileInfo.getFileName();
            int index=fName.lastIndexOf(".");
            //String fileSuffix=getFileSuffix(fileInfo.getFileName());
            String fileSuffix=( index!=-1 ? fName.substring(index) : "" );
            String month= DateUtil.format(fileInfo.getCreateTime(), DateTimePatternEnum.YYYY_MM.getPattern());

            //创建目标目录
            String targetFolderBasePath=baseConfig.getProjectFolder()+Constants.FILE_PATH+userDto.getUid();
            File targetFolder=new File(targetFolderBasePath+"/"+month);
            if(!targetFolder.exists()){
                targetFolder.mkdirs();
            }
            //真实存储的文件名
            String fileName=currentUserFolderName+fileSuffix;
            targetFilePath=targetFolder.getPath()+"/"+fileName;

            //合并文件
            unionFiles(fileFolder.getPath(),targetFilePath,fileInfo.getFileName(),true);

            fileTypeEnum=FileTypeEnum.getFileTypeBySuffix(fileSuffix);
            //视频文件切割，同时生成缩略图
            if(fileTypeEnum==FileTypeEnum.VIDEO){
                //视频文件切割，预览播放时使用切割文件进行播放
                cutVideoFile(fileId, targetFilePath);
                //生成视频缩略图
                cover=currentUserFolderName+".png";
                String coverPath=targetFolder.getPath()+"/"+cover;
                ScaleFilter.createCover4Video(new File(targetFilePath),150,new File(coverPath));
            }
            //图像缩略图生成
            else if(fileTypeEnum==FileTypeEnum.IMAGE){
                cover=fileName.replace(".","_.");
                String coverPath=targetFolder.getPath()+"/"+cover;
                File targetPicFile=new File(targetFilePath);
                File coverFile=new File(coverPath);
                Boolean success=ScaleFilter.createCover4Pic(targetPicFile,150,coverFile,false);
                if(!success){
                    //图太小就把原图复制给缩略图
                    FileUtils.copyFile(targetPicFile,coverFile);
                }
            }
            //删除redis存储的文件临时大小
            String sizeKey = Constants.REDIS_USER_FILE_TEMP_SIZE + userDto.getUid() + fileId;
            redisUtils.delete(sizeKey);

        }catch (Exception e){
            logger.error("文件转码失败，文件ID:{}，userid:{}",fileId,userDto.getUid(),e);
            transferSuccess=false;
        }finally {
            FileInfo updateFile=new FileInfo();
            updateFile.setFileCover(cover);
            updateFile.setStatus(transferSuccess ? FileStatusEnum.SUCCESS.getStatus() : FileStatusEnum.FAIL.getStatus());
            fileMapper.updateStatusWithOldStatus(fileId,userDto.getUid(),updateFile,FileStatusEnum.TRANSFER.getStatus());
        }
    }

    private void unionFiles(String dirPath,String toFilePath,String fileName,Boolean isDel){
        File dir=new File(dirPath);
        if(!dir.exists()){
            throw new BusinessException("temp目录不存在");
        }
        File[] files=dir.listFiles();
        File targetFile =new File(toFilePath);	//创建目标文件
        BufferedOutputStream writeFile=null;
        try{
            writeFile=new BufferedOutputStream(new FileOutputStream(targetFile));  //读写
            //一次读的数据大小
            byte[] bytes=new byte[1024*10];
            //依次读取分片文件
            for(int i=0;i<files.length;i++){
                int len=-1;
                File chunkFile=new File(dirPath+"/"+i);
                //RandomAccessFile readFile=null;
                BufferedInputStream readFile=null;
                try {
                    //readFile=new RandomAccessFile(chunkFile,"r");
                    readFile=new BufferedInputStream(new FileInputStream(chunkFile));
                    //将最多 bytes.length 个数据字节从此文件读入bytes数组。
                    while((len=readFile.read(bytes))!=-1){
                        //将读取数据写入文件
                        ////从偏移量off处开始，将len个字节从bytes数组写入到此文件writeFile。
                        writeFile.write(bytes,0,len);
                    }
                }catch (Exception e){
                    logger.error("读取分片文件失败",e);
                }finally {
                    readFile.close();
                }
            }
            //删除临时文件
            if(isDel&&dir.exists()){
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }catch (Exception e){
            logger.error("合并文件:{} 失败",fileName,e);
            throw new BusinessException("合并文件"+fileName+"失败");
        }finally {
            //关闭IO
            if(writeFile!=null){
                try {
                    writeFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 视频切割方法
     * @param videoFilePath:视频文件路径
     */
    private void cutVideoFile(String fileId,String videoFilePath){
        //创建同名切片目录
        File tsFile=new File(videoFilePath.substring(0,videoFilePath.lastIndexOf(".")));
        if(!tsFile.exists()){
            tsFile.mkdirs();
        }
        //ffmpeg命令:先把视频文件转为ts文件然后再进行切割
        String CMD_TRANSFER_2TS=Constants.CMD_TRANSFER_2TS;
        String CMD_CUT_TS=Constants.CMD_CUT_TS;
        String tsPath= tsFile.getPath()+"/"+Constants.TS_NAME;
        //上传视频的编码格式必须是h264，否则不能转换为ts文件
        //转为ts文件,播放的时候也是切片播放的
        String cmd=String.format(CMD_TRANSFER_2TS,videoFilePath,tsPath);
        FfmpegUtils.executeCommand(cmd,false);
        //生成索引文件.m3u8和切片文件.ts
        String m3u8Path=tsFile.getPath()+"/"+Constants.M3U8_NAME;
        cmd=String.format(CMD_CUT_TS,tsPath,m3u8Path,tsFile.getPath(),fileId);
        FfmpegUtils.executeCommand(cmd,false);
        //删除转换的index.ts文件
        new File(tsPath).delete();
    }
}
