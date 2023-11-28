package com.wangpan.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileMapper<T, P> extends BaseMapper{
	/** 
	 * 根据 FidAndUserId 查询
	 */
	T selectByFidAndUserId(@Param("fid") String fid, @Param("userId") String userId) ;

	/** 
	 * 根据 FidAndUserId 更新
	 */
	Integer updateByFidAndUserId(@Param("bean") T t, @Param("fid") String fid, @Param("userId") String userId) ;

	/** 
	 * 根据 FidAndUserId 删除
	 */
	Integer deleteByFidAndUserId(@Param("fid") String fid, @Param("userId") String userId) ;

	/**
	 * 查询已使用空间大小/已上传文件总大小
	 */
	Long getUsedSpaceByUid(@Param("userId") String userId);
}