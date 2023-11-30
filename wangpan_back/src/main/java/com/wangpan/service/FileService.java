package com.wangpan.service;

import com.wangpan.dto.UploadResultDto;
import com.wangpan.dto.UserDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.query.FileQuery;
import com.wangpan.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface FileService {
	/** 
	 * 根据条件查询列表
	 */
	List<FileInfo> findListByParam(FileQuery query);

	/** 
	 * 根据条件查询数量
	 */
	Integer  findCountByParam(FileQuery query);

	/** 
	 * 分页查询
	 */
	PaginationResultVO<FileInfo> findListByPage(FileQuery query);

	/** 
	 * 新增
	 */
	Integer add(FileInfo bean);

	/** 
	 * 批量新增
	 */
	Integer addBatch(List<FileInfo> beanList);

	/** 
	 * 批量修改
	 */
	Integer addOrUpdateBatch(List<FileInfo> beanList);

	/** 
	 * 根据 FidAndUserId 查询
	 */
	FileInfo getFileByFidAndUserId(String fid, String userId) ;

	/** 
	 * 根据 FidAndUserId 更新
	 */
	Integer updateFileByFidAndUserId(FileInfo bean, String fid, String userId) ;

	/** 
	 * 根据 FidAndUserId 删除
	 */
	Integer deleteFileByFidAndUserId(String fid, String userId) ;

	/**
	 * 上传文件
	 */
	UploadResultDto uploadFile(UserDto userDto, String fileId, MultipartFile file, String fileName,
							   String filePid, String fileMd5, Integer chunkIndex, Integer chunks);

	/**
	 * 获取文件后缀名
	 */
	String getFileSuffix(String fileName);

	/**
	 * 获取特定文件的路径
	 */
	String findFilePath(String baseFilePath, String imageName);
}