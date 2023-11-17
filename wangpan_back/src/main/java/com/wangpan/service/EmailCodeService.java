package com.wangpan.service;

import com.wangpan.entity.po.EmailCode;
import com.wangpan.entity.query.EmailCodeQuery;
import com.wangpan.entity.vo.PaginationResultVO;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface EmailCodeService {
	/** 
	 * 根据条件查询列表
	 */
	List<EmailCode> findListByParam(EmailCodeQuery query);

	/** 
	 * 根据条件查询数量
	 */
	Integer  findCountByParam(EmailCodeQuery query);

	/** 
	 * 分页查询
	 */
	PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery query);

	/** 
	 * 新增
	 */
	Integer add(EmailCode bean);

	/** 
	 * 批量新增
	 */
	Integer addBatch(List<EmailCode> beanList);

	/** 
	 * 批量修改
	 */
	Integer addOrUpdateBatch(List<EmailCode> beanList);

	/** 
	 * 根据 EmailAndCode 查询
	 */
	EmailCode getEmailCodeByEmailAndCode(String email, String code) ;

	/** 
	 * 根据 EmailAndCode 更新
	 */
	Integer updateEmailCodeByEmailAndCode( EmailCode bean, String email, String code) ;

	/** 
	 * 根据 EmailAndCode 删除
	 */
	Integer deleteEmailCodeByEmailAndCode(String email, String code) ;

    void sendEmailCode(String email, Integer type);

	/**
	 * 校验邮箱验证码
	 */
	void checkCode(String email,String code);
}