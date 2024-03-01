package com.wangpan.service;


public interface EmailCodeService {

    void sendEmailCode(String email, Integer type);

	/**
	 * 校验邮箱验证码
	 */
	void checkCode(String email,String code);
}