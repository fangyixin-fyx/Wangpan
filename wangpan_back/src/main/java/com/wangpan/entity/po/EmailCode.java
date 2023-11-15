package com.wangpan.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class EmailCode implements Serializable{
	private String email;
	// 5个随机验证码
	private String code;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	// 0:未使用 1:已使用
	private Integer status;

	public void setEmail(String email){  this.email=email; }
	public String getEmail(){  return this.email; }

	public void setCode(String code){  this.code=code; }
	public String getCode(){  return this.code; }

	public void setCreateTime(Date createTime){  this.createTime=createTime; }
	public Date getCreateTime(){  return this.createTime; }

	public void setStatus(Integer status){  this.status=status; }
	public Integer getStatus(){  return this.status; }

	@Override
	public String toString(){
		return "	email:"+this.email+"	code:"+this.code+"	create_time:"+this.createTime+"	status:"+this.status;
	}

}