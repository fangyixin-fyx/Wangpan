package com.wangpan.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailCodeMapper<T, P> extends BaseMapper{
	/** 
	 * 根据 EmailAndCode 查询
	 */
	T selectByEmailAndCode(@Param("email") String email, @Param("code") String code) ;

	/** 
	 * 根据 EmailAndCode 更新
	 */
	Integer updateByEmailAndCode(@Param("bean") T t, @Param("email") String email, @Param("code") String code) ;

	/** 
	 * 根据 EmailAndCode 删除
	 */
	Integer deleteByEmailAndCode(@Param("email") String email, @Param("code") String code) ;


    void disableEmailCode(@Param("email") String email);
}