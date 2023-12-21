package com.wangpan.service;

import com.wangpan.dto.FileShareDto;
import com.wangpan.entity.po.FileShare;
import com.wangpan.entity.query.FileShareQuery;
import com.wangpan.entity.vo.PaginationResultVO;

import java.util.List;

public interface FileShareService {
	/** 
	 * 根据条件查询列表
	 */
	List<FileShare> findListByParam(FileShareQuery query);

	/** 
	 * 根据条件查询数量
	 */
	Integer  findCountByParam(FileShareQuery query);

	/** 
	 * 分页查询
	 */
	PaginationResultVO<FileShareDto> findListByPage(FileShareQuery query);

	/** 
	 * 新增
	 */
	Integer add(FileShare bean);

	/** 
	 * 批量新增
	 */
	Integer addBatch(List<FileShare> beanList);

	/** 
	 * 批量修改
	 */
	Integer addOrUpdateBatch(List<FileShare> beanList);

	/** 
	 * 根据 ShareId 查询
	 */
	FileShare getFileShareByShareId(String shareId) ;

	/** 
	 * 根据 ShareId 更新
	 */
	Integer updateFileShareByShareId( FileShare bean, String shareId) ;

	/** 
	 * 根据 ShareId 删除
	 */
	Integer deleteFileShareByShareId(String shareId) ;

	/**
	 * 分享文件
	 */
	FileShare shareFile(FileShareQuery fileShareQuery);

	void deleteFileShareBatch(String shareIds);
}