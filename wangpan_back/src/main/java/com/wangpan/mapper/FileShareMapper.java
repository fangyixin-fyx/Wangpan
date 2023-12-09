package com.wangpan.mapper;

import com.wangpan.dto.FileShareDto;
import com.wangpan.dto.WebShareInfoDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.po.FileShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface FileShareMapper<T, P> extends BaseMapper{
	/** 
	 * 根据 ShareId 查询
	 */
	FileShare selectByShareId(@Param("shareId") String shareId) ;

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

	/**
	 * 外部获取分享文件数据
	 */
	WebShareInfoDto getWebShareInfo(@Param("shareId") String shareId);

	void updateShowCountByShareId(@Param("shareId") String shareId);


    List<FileShare> selectExpiredFile(@Param("currTime") Date currTime);

	void cleanByShareId(@Param("shareId") String shareId);
}