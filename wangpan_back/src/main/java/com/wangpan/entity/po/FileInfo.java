package com.wangpan.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class FileInfo implements Serializable{

	@JsonProperty("fileId")
	private String fid;
	private String userId;
	// 文件MD5值
	private String fileMd5;
	// 父级ID
	private String filePid;
	// 文件大小
	private Long fileSize;
	// 文件名
	private String fileName;
	// 文件封面
	private String fileCover;
	// 文件路径
	private String filePath;
	// 创建时间
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	// 最后更新时间
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastUpdateTime;
	// 0：文件   1：目录
	private Integer folderType;
	// 文件分类 1:视频 2:音频 3:图片 4:文档  5:其他
	private Integer fileCategory;
	// 1:视频  2:音频 3:图片 4:pdf  5:doc 6:excel  7:txt  8:code 9:zip 10:其他
	private Integer fileType;
	// 状态 0：转码中 1：转码失败  2：转码成功
	private Integer status;
	// 进入回收站时间
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date recoveryTime;
	// 0：删除   1：回收站   2：正常
	private Integer delFlag;

	//管理员查看文件使用者
	private String nickName;

	@Override
	public String toString(){
		return "	fileId:"+this.fid+"	userId:"+this.userId+"	fileMd5:"+this.fileMd5+"	filePid:"+this.filePid+"	fileSize:"+this.fileSize+"	fileName:"+this.fileName+"	fileCover:"+this.fileCover+"	filePath:"+this.filePath+"	createTime:"+this.createTime+"	lastUpdateTime:"+this.lastUpdateTime+"	folderType:"+this.folderType+"	category:"+this.fileCategory+"	fileType:"+this.fileType+"	status:"+this.status+"	recoveryTime:"+this.recoveryTime+"	delFlag:"+this.delFlag;
	}

}