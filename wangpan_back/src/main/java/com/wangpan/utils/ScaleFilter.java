package com.wangpan.utils;

import com.wangpan.exception.BusinessException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 通过ffmpeg生成封面
 * @author fangyixin
 * @date 2023/11/30 15:36
 */
public class ScaleFilter {
    private static final Logger logger= LoggerFactory.getLogger(ScaleFilter.class);

    public static void createCover4Video(File sourceFile, Integer width, File targetFile){
        //从输入视频文件中提取第一帧，并进行大小调整%d*%d，生成一张静态图片。
        String cmd="ffmpeg -i %s -y -vframes 1 -vf scale=%d:%d/a %s";
        try{
            FfmpegUtils.executeCommand(String.format(cmd,sourceFile.getAbsoluteFile(),width,width,
                    targetFile.getAbsoluteFile()),false);
        }catch (Exception e){
            throw new BusinessException("生成视频封面失败",e);
        }
    }

    public static Boolean createCover4Pic(File file,Integer width,File targetFile,Boolean delSource){
        try {
            BufferedImage src= ImageIO.read(file);
            int source_width=src.getWidth();
            int source_height=src.getHeight();
            //如果原图大小 小于 压缩图大小 就不压缩
            if(source_width<=width){
                return false;
            }
            //压缩图像
            compressImage(file,width,targetFile,delSource);
            return true;
        }catch (Exception e){
            logger.error("生成缩略图失败",e);
        }
        return false;
    }

    private static void compressImage(File file,Integer width,File targetFile,Boolean delSource){
        String cmd="ffmpeg -i %s -vf scale=%d:-1 %s -y";
        try {
            FfmpegUtils.executeCommand(String.format(cmd,file.getAbsoluteFile(),width,targetFile.getAbsoluteFile()),
                    false);
            if(delSource){
                FileUtils.forceDelete(file);
            }
        }catch (Exception e){
            logger.error("压缩图片失败");
        }
    }
}
