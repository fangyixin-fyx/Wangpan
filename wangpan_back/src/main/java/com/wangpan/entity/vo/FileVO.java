package com.wangpan.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 返回给前端的精简信息
 * @author fangyixin
 * @date 2023/11/27 14:26
 */
@Data
public class FileVO {
    private String fid;
    //父级ID
    private String fPid;
    private Long fileSize;
    private String fileName;
    //封面
    private String fileCover;
    //最后更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;

    private Integer folderType; // 0:文件 1：目录
    private Integer fileCategory; //1：视频 2：音频 3：图片 4：文档 5：其他
    private Integer fileType; // 1：视频 2：音频 3：图片 4：PDF 5：doc 6：excel 7：txt 8：code 9：zip 10：其他
    private Integer status; // 0：转码中  1：转码失败 2：转码成功


}
