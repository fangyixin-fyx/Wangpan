package com.wangpan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

@Data
@Component
public class FileShareDto implements Serializable{
	private String shareId;
	private String fileId;
	private String userId;
	// 有效期  0：1天  1：7天   2：30天   3：永久
	private Integer validType;
	// 失效时间
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date expireTime;
	// 分享时间
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date shareTime;
	// 提取码
	private String code;
	// 被浏览次数
	private Integer showCount;

	private String fileName;
	private String fileCover;
	private String fileCategory;
	private String fileType;
	private String folderType;



}