package com.wangpan.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable{
	// ID
	private String uid;
	// 用户名
	private String username;
	// 密码(使用MD5)
	private String password;
	// 注册邮箱
	private String email;
	private String qqOpenID;
	// QQ头像
	private String qqIcon;
	// 注册时间
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date registrationTime;
	// 最近登陆时间
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;
	// 账号状态 0：禁用   1：可用
	private Integer state;
	// 使用空间
	private Long useSpace;
	// 总空间
	private Long totalSpace;

	public void setUid(String uid){  this.uid=uid; }
	public String getUid(){  return this.uid; }

	public void setUsername(String username){  this.username=username; }
	public String getUsername(){  return this.username; }

	public void setPassword(String password){  this.password=password; }
	public String getPassword(){  return this.password; }

	public void setEmail(String email){  this.email=email; }
	public String getEmail(){  return this.email; }

	public void setQqOpenID(String qqOpenID){  this.qqOpenID=qqOpenID; }
	public String getQqOpenID(){  return this.qqOpenID; }

	public void setQqIcon(String qqIcon){  this.qqIcon=qqIcon; }
	public String getQqIcon(){  return this.qqIcon; }

	public void setRegistrationTime(Date registrationTime){  this.registrationTime=registrationTime; }
	public Date getRegistrationTime(){  return this.registrationTime; }

	public void setLastLoginTime(Date lastLoginTime){  this.lastLoginTime=lastLoginTime; }
	public Date getLastLoginTime(){  return this.lastLoginTime; }

	public void setState(Integer state){  this.state=state; }
	public Integer getState(){  return this.state; }

	public void setUseSpace(Long useSpace){  this.useSpace=useSpace; }
	public Long getUseSpace(){  return this.useSpace; }

	public void setTotalSpace(Long totalSpace){  this.totalSpace=totalSpace; }
	public Long getTotalSpace(){  return this.totalSpace; }

	@Override
	public String toString(){
		return "	uid:"+this.uid+"	username:"+this.username+"	password:"+this.password+"	email:"+this.email+"	qqOpenID:"+this.qqOpenID+"	qqIcon:"+this.qqIcon+"	registrationTime:"+this.registrationTime+"	lastLoginTime:"+this.lastLoginTime+"	state:"+this.state+"	useSpace:"+this.useSpace+"	totalSpace:"+this.totalSpace;
	}

}