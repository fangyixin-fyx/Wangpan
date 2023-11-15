package com.wangpan.entity.query;

import java.util.Date;

public class UserQuery extends BaseQuery {
	// ID
	private String uid;
	private String uidFuzzy;
	// 用户名
	private String username;
	private String usernameFuzzy;
	// 密码(使用MD5)
	private String password;
	private String passwordFuzzy;
	// 注册邮箱
	private String email;
	private String emailFuzzy;
	private String qqOpenID;
	private String qqOpenIDFuzzy;
	// QQ头像
	private String qqIcon;
	private String qqIconFuzzy;
	// 注册时间
	private Date registrationTime;
	private String registrationTimeStart;
	private String registrationTimeEnd;
	// 最近登陆时间
	private Date lastLoginTime;
	private String lastLoginTimeStart;
	private String lastLoginTimeEnd;
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

	public void setUidFuzzy(String uidFuzzy){  this.uidFuzzy=uidFuzzy; }
	public String getUidFuzzy(){  return this.uidFuzzy; }

	public void setUsernameFuzzy(String usernameFuzzy){  this.usernameFuzzy=usernameFuzzy; }
	public String getUsernameFuzzy(){  return this.usernameFuzzy; }

	public void setPasswordFuzzy(String passwordFuzzy){  this.passwordFuzzy=passwordFuzzy; }
	public String getPasswordFuzzy(){  return this.passwordFuzzy; }

	public void setEmailFuzzy(String emailFuzzy){  this.emailFuzzy=emailFuzzy; }
	public String getEmailFuzzy(){  return this.emailFuzzy; }

	public void setQqOpenIDFuzzy(String qqOpenIDFuzzy){  this.qqOpenIDFuzzy=qqOpenIDFuzzy; }
	public String getQqOpenIDFuzzy(){  return this.qqOpenIDFuzzy; }

	public void setQqIconFuzzy(String qqIconFuzzy){  this.qqIconFuzzy=qqIconFuzzy; }
	public String getQqIconFuzzy(){  return this.qqIconFuzzy; }

	public void setRegistrationTimeStart(String registrationTimeStart){  this.registrationTimeStart=registrationTimeStart; }
	public String getRegistrationTimeStart(){  return this.registrationTimeStart; }

	public void setRegistrationTimeEnd(String registrationTimeEnd){  this.registrationTimeEnd=registrationTimeEnd; }
	public String getRegistrationTimeEnd(){  return this.registrationTimeEnd; }

	public void setLastLoginTimeStart(String lastLoginTimeStart){  this.lastLoginTimeStart=lastLoginTimeStart; }
	public String getLastLoginTimeStart(){  return this.lastLoginTimeStart; }

	public void setLastLoginTimeEnd(String lastLoginTimeEnd){  this.lastLoginTimeEnd=lastLoginTimeEnd; }
	public String getLastLoginTimeEnd(){  return this.lastLoginTimeEnd; }

}