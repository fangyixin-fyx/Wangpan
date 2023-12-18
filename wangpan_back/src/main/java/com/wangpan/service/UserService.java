package com.wangpan.service;

import com.wangpan.dto.UserDto;
import com.wangpan.dto.UserForAdminDto;
import com.wangpan.entity.po.User;
import com.wangpan.entity.query.UserQuery;
import com.wangpan.entity.vo.PaginationResultVO;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;

public interface UserService {
	/** 
	 * 根据条件查询列表
	 */
	List<User> findListByParam(UserQuery query);

	/** 
	 * 根据条件查询数量
	 */
	Integer  findCountByParam(UserQuery query);

	/** 
	 * 分页查询
	 */
	PaginationResultVO<User> findListByPage(UserQuery query);

	/** 
	 * 新增
	 */
	Integer add(User bean);

	/** 
	 * 批量新增
	 */
	Integer addBatch(List<User> beanList);

	/** 
	 * 批量修改
	 */
	Integer addOrUpdateBatch(List<User> beanList);

	/** 
	 * 根据 Uid 查询
	 */
	User getUserByUid(String uid) ;

	/** 
	 * 根据 Uid 更新
	 */
	Integer updateUserByUid( User bean, String uid) ;

	/** 
	 * 根据 Uid 删除
	 */
	Integer deleteUserByUid(String uid) ;

	/** 
	 * 根据 Email 查询
	 */
	User getUserByEmail(String email) ;

	/** 
	 * 根据 Email 更新
	 */
	Integer updateUserByEmail( User bean, String email) ;

	/** 
	 * 根据 Email 删除
	 */
	Integer deleteUserByEmail(String email) ;

	/** 
	 * 根据 QqOpenID 查询
	 */
	User getUserByQqOpenID(String qqOpenID) ;

	/** 
	 * 根据 QqOpenID 更新
	 */
	Integer updateUserByQqOpenID( User bean, String qqOpenID) ;

	/** 
	 * 根据 QqOpenID 删除
	 */
	Integer deleteUserByQqOpenID(String qqOpenID) ;

	/** 
	 * 根据 Username 查询
	 */
	User getUserByUsername(String username) ;

	/** 
	 * 根据 Username 更新
	 */
	Integer updateUserByUsername( User bean, String username) ;

	/** 
	 * 根据 Username 删除
	 */
	Integer deleteUserByUsername(String username) ;

	/**
	 * 注册
	 */
	void register(String username, String password,String email,String emailCode);

	UserDto login(String email, String password);

	/**
	 * 重置密码
	 * @param password：新密码
	 * @param emailCode：邮箱验证码
	 */
	void resetPwd(String email,String password,String emailCode);

	/**
	 * 管理员查看用户信息
	 */
	PaginationResultVO<UserForAdminDto> adminGetUserListByPage(UserQuery query);

	/**
	 * 管理员更新用户可用空间
	 */
	int updateUserSpace(String userId,String changeSpace,String currUser);

	/**
	 * 管理员修改用户账号状态
	 * @param userId：被修改状态的用户
	 * @param currUid:当前登录用户
	 * @return
	 */
	int updateStatusByAdmin(String userId, String status, String currUid);

	/**
	 * 获取头像
	 * @param userId
	 * @return：头像存储路径
	 */
    String getAvatar(String userId);

	void logout(HttpSession session);
}