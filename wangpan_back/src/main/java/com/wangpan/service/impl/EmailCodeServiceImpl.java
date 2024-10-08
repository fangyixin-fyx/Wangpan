package com.wangpan.service.impl;

import com.wangpan.config.BaseConfig;
import com.wangpan.constants.Constants;
import com.wangpan.entity.po.User;
import com.wangpan.entity.query.UserQuery;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.UserMapper;
import com.wangpan.service.EmailCodeService;
import com.wangpan.utils.RedisUtils;
import com.wangpan.utils.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.util.Date;

@Service("emailCodeService")
public class EmailCodeServiceImpl implements EmailCodeService {
	private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);
	@Autowired
	private UserMapper<User, UserQuery> userMapper;
	//邮箱配置
	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private BaseConfig baseConfig;
	@Autowired
	private RedisUtils redisUtils;


	/**
	 * 发送验证码
	 * @param email：邮箱
	 * @param type：验证码有效检验
	 */
	@Override
	@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
	public void sendEmailCode(String email, Integer type){
		if(type==0){
			User user=userMapper.selectByEmail(email);
			if(user!=null) throw new BusinessException("邮箱已被注册，请更换注册邮箱！");
		}
		//获得随机数
		String code = StringTool.getRandomNumber(Constants.LENGTH_5);

		//存入redis，有效时间5min
		boolean res=redisUtils.setByTime(Constants.EMAIL_CODE+email,code,300L);
		if(!res){
			throw new BusinessException("redis保存验证码不成功");
		}
		//发送验证码
		sendCode(email,code);

	}

	//发送验证码到邮箱
	private void sendCode(String email, String code){
		try{
			MimeMessage message=javaMailSender.createMimeMessage();
			MimeMessageHelper helper=new MimeMessageHelper(message,true);
			helper.setFrom(baseConfig.getMailUsername()); //发件人
			helper.setTo(email); //收件人
			//设置邮箱标题
			helper.setSubject(Constants.EMAIL_TITLE);
			//设置邮件内容
			helper.setText(String.format(Constants.EMAIL_CONTEXT,code));
			//邮件时间
			helper.setSentDate(new Date());

			javaMailSender.send(message);
		}catch (Exception e){
			logger.error("邮件发送失败",e);
			throw new BusinessException("邮件发送失败");
		}

	}

	/**
	 * 检查验证码是否正确
	 */
	public void checkCode(String email,String code){
		/*
		EmailCode emailCode=emailCodeMapper.selectByEmailAndCode(email,code);
		if(emailCode==null) throw new BusinessException("验证码错误");
		if(emailCode.getStatus()==1 ||
			System.currentTimeMillis()-emailCode.getCreateTime().getTime()>Constants.LENGTH_15*1000*60){ //15min限制
			throw new BusinessException("验证码已失效");
		}
		//验证码正确则将其标为失效，已使用
		emailCodeMapper.disableEmailCode(email);

		 */
		String redisKey=Constants.EMAIL_CODE+email;
		String correctCode= (String) redisUtils.get(redisKey);
		if(correctCode==null){
			throw new BusinessException("邮箱验证码已失效或不存在，请重新获取");
		}
		if(!correctCode.equals(code)){
			throw new BusinessException("邮箱验证码错误");
		}
		//验证码正确，清除redis缓存
		redisUtils.delete(redisKey);
	}


}