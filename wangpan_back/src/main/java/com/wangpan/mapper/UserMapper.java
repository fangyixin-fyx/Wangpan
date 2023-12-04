package com.wangpan.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper<T, P> extends BaseMapper{
	/** 
	 * 根据 Uid 查询
	 */
	T selectByUid(@Param("uid") String uid) ;

	/** 
	 * 根据 Uid 更新
	 */
	Integer updateByUid(@Param("bean") T t, @Param("uid") String uid) ;

	/** 
	 * 根据 Uid 删除
	 */
	Integer deleteByUid(@Param("uid") String uid) ;

	/** 
	 * 根据 Email 查询
	 */
	T selectByEmail(@Param("email") String email) ;

	/** 
	 * 根据 Email 更新
	 */
	Integer updateByEmail(@Param("bean") T t, @Param("email") String email) ;

	/** 
	 * 根据 Email 删除
	 */
	Integer deleteByEmail(@Param("email") String email) ;

	/** 
	 * 根据 QqOpenID 查询
	 */
	T selectByQqOpenID(@Param("qqOpenID") String qqOpenID) ;

	/** 
	 * 根据 QqOpenID 更新
	 */
	Integer updateByQqOpenID(@Param("bean") T t, @Param("qqOpenID") String qqOpenID) ;

	/** 
	 * 根据 QqOpenID 删除
	 */
	Integer deleteByQqOpenID(@Param("qqOpenID") String qqOpenID) ;

	/** 
	 * 根据 Username 查询
	 */
	T selectByUsername(@Param("username") String username) ;

	/** 
	 * 根据 Username 更新
	 */
	Integer updateByUsername(@Param("bean") T t, @Param("username") String username) ;

	/** 
	 * 根据 Username 删除
	 */
	Integer deleteByUsername(@Param("username") String username) ;

	/**
	 * 更新空间：累加
	 */
	Integer updateUserSpace(@Param("uid") String uid,@Param("useSpace") Long useSpace,@Param("totalSpace") Long totalSpace);

	/**
	 * 更新空间
	 */
	Integer updateUserSpace2(@Param("uid") String uid,@Param("useSpace") Long useSpace,@Param("totalSpace") Long totalSpace);

}