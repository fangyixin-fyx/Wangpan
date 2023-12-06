package com.wangpan.mapper;

import com.wangpan.dto.FileShareDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileShareMapper<T, P> extends BaseMapper{
	/** 
	 * 根据 ShareId 查询
	 */
	T selectByShareId(@Param("shareId") String shareId) ;

	/** 
	 * 根据 ShareId 更新
	 */
	Integer updateByShareId(@Param("bean") T t, @Param("shareId") String shareId) ;

	/** 
	 * 根据 ShareId 删除
	 */
	Integer deleteByShareId(@Param("shareId") String shareId) ;

	/**
	 * 根据id数组进行批量删除
	 * @param idArray
	 */
    int deleteBatchByShareId(@Param("shareIdArray") String[] idArray);

	/**
	 * 查询集合返回DTO
	 */
	List<FileShareDto> selectListByUid(@Param("query") P p);
}