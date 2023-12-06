package com.wangpan.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class FileShare implements Serializable{
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

	public void setShareId(String shareId){  this.shareId=shareId; }
	public String getShareId(){  return this.shareId; }

	public void setFileId(String fileId){  this.fileId=fileId; }
	public String getFileId(){  return this.fileId; }

	public void setUserId(String userId){  this.userId=userId; }
	public String getUserId(){  return this.userId; }

	public void setValidType(Integer validType){  this.validType=validType; }
	public Integer getValidType(){  return this.validType; }

	public void setExpireTime(Date expireTime){  this.expireTime=expireTime; }
	public Date getExpireTime(){  return this.expireTime; }

	public void setShareTime(Date shareTime){  this.shareTime=shareTime; }
	public Date getShareTime(){  return this.shareTime; }

	public void setCode(String code){  this.code=code; }
	public String getCode(){  return this.code; }

	public void setShowCount(Integer showCount){  this.showCount=showCount; }
	public Integer getShowCount(){  return this.showCount; }

	@Override
	public String toString(){
		return "	shareId:"+this.shareId+"	fileId:"+this.fileId+"	userId:"+this.userId+"	validType:"+this.validType+"	expireTime:"+this.expireTime+"	shareTime:"+this.shareTime+"	code:"+this.code+"	showCount:"+this.showCount;
	}

}