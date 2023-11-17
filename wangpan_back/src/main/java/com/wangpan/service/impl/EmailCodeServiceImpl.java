package com.wangpan.service.impl;

import com.wangpan.config.BaseConfig;
import com.wangpan.config.RedisConfig;
import com.wangpan.constants.Constants;
import com.wangpan.dto.SysSettingsDto;
import com.wangpan.entity.po.EmailCode;
import com.wangpan.entity.po.User;
import com.wangpan.entity.query.EmailCodeQuery;
import com.wangpan.entity.query.SimplePage;
import com.wangpan.entity.query.UserQuery;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.enums.PageSize;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.EmailCodeMapper;
import com.wangpan.mapper.UserMapper;
import com.wangpan.service.EmailCodeService;
import com.wangpan.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;

@Service("emailCodeService")
public class EmailCodeServiceImpl implements EmailCodeService {
	private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);
	@Autowired
	private EmailCodeMapper<EmailCode,EmailCodeQuery> emailCodeMapper;
	@Autowired
	private UserMapper<User, UserQuery> userMapper;
	//邮箱配置
	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private BaseConfig baseConfig;
	@Autowired
	private RedisConfig redisConfig;

	/** 
	 * 根据条件查询列表
	 */
	@Override
	public List<EmailCode> findListByParam(EmailCodeQuery query){
		return emailCodeMapper.selectList(query);
	}

	/** 
	 * 根据条件查询数量
	 */
	@Override
	public Integer  findCountByParam(EmailCodeQuery query){
		return emailCodeMapper.selectCount(query);
	}

	/** 
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery query){
		int count=Math.toIntExact(this.findCountByParam(query));
		int pageSize= query.getPageSize()==null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<EmailCode> list=this.findListByParam(query);
		PaginationResultVO<EmailCode> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/** 
	 * 新增
	 */
	@Override
	public Integer add(EmailCode bean){
		return emailCodeMapper.insert(bean);
	}

	/** 
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<EmailCode> beanList){
		if(beanList==null || beanList.isEmpty()) { return 0; }
		return emailCodeMapper.insertBatch(beanList);
	}

	/** 
	 * 批量修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<EmailCode> beanList){
		if(beanList==null || beanList.isEmpty()) { return 0; }
		return emailCodeMapper.insertOrUpdateBatch(beanList);
	}

	/** 
	 * 根据 EmailAndCode 查询
	 */
	@Override
	public EmailCode getEmailCodeByEmailAndCode(String email, String code){
		return emailCodeMapper.selectByEmailAndCode(email, code);
	}


	/** 
	 * 根据 EmailAndCode 更新
	 */
	@Override
	public Integer updateEmailCodeByEmailAndCode( EmailCode bean, String email, String code){
		return emailCodeMapper.updateByEmailAndCode(bean,email, code);
	}


	/** 
	 * 根据 EmailAndCode 删除
	 */
	@Override
	public Integer deleteEmailCodeByEmailAndCode(String email, String code){
		return emailCodeMapper.deleteByEmailAndCode(email, code);
	}

	/**
	 * 发送验证码
	 * @param email：邮箱
	 * @param type：验证码有效检验
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sendEmailCode(String email, Integer type){
		if(type==0){
			User user=userMapper.selectByEmail(email);
			if(user!=null) throw new BusinessException("邮箱已存在！");
		}
		//获得随机数
		String code = StringUtil.getRandomNumber(Constants.LENGTH_5);
		//重置已有验证码，置为无效
		emailCodeMapper.disableEmailCode(email);
		// 发送验证码
		sendCode(email,code);
		//记入数据库
		EmailCode emailCode=new EmailCode();
		emailCode.setCode(code);
		emailCode.setEmail(email);
		emailCode.setStatus(Constants.ZERO);
		emailCode.setCreateTime(new Date());
		emailCodeMapper.insert(emailCode);

	}

	//发送验证码到邮箱
	private void sendCode(String email, String code){
		try{
			MimeMessage message=javaMailSender.createMimeMessage();
			MimeMessageHelper helper=new MimeMessageHelper(message,true);
			helper.setFrom(baseConfig.getMailUsername()); //发件人
			helper.setTo(email); //收件人

			SysSettingsDto sysSettingsDto=redisConfig.getSysSettingDto();
			//设置邮件标题
			helper.setSubject(sysSettingsDto.getRegisterEmailTitle());
			//设置邮件内容
			helper.setText(String.format(sysSettingsDto.getRegisterEmailContent(),code));
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
		EmailCode emailCode=emailCodeMapper.selectByEmailAndCode(email,code);
		if(emailCode==null) throw new BusinessException("验证码错误");
		if(emailCode.getStatus()==1 ||
			System.currentTimeMillis()-emailCode.getCreateTime().getTime()>Constants.LENGTH_15*1000*60){ //15min限制
			throw new BusinessException("验证码已失效");
		}
		//验证码正确则将其标为失效，已使用
		emailCodeMapper.disableEmailCode(email);
	}


}