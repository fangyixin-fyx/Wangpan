package com.wangpan.mapper;

import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.query.FileQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


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

	/**
	 * 更新状态，乐观锁
	 */
	void updateStatusWithOldStatus(@Param("fid") String fileId,@Param("uid") String userId,@Param("bean") T t,@Param("oldStatus") Integer oldStatus);

	List<FileInfo> selectByFileMd5(@Param("fileMd5") String fileMd5);

	FileInfo selectByFid(@Param(("fid")) String fileId);

    void updateFileByFid(@Param("bean") T t, @Param("fid") String fid);

	List<String> getFileNameByPid(@Param("pid") String pid);

	/**
	 * 移入回收站，并没有真正删除
	 */
	void delByFid(@Param("fid") String fid, @Param("delFlag") Integer delFlag, @Param("currTime")Date currTime);

	List<FileInfo> selectFileByUidAndPid(@Param("uid") String uid,@Param("pid") String pid);

	void deleteCompletelyByFid(@Param("fids") String[] fids);

    List<FileInfo> selectListByAdmin(@Param("bean") P query);

	List<FileInfo> selectForShareFile(@Param("fileId")String fileId,@Param("filePid")String filePid,@Param("orderBy") String orderBy);

	List<FileInfo> selectShareSubFile(@Param("filePid") String filePid);

	/**
	 * 获取根目录分享文件信息
	 */
	List<FileInfo> selectFileList(@Param("shareId") String shareId);
}