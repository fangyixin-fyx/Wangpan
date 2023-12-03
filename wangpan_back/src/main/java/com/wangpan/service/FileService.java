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

    void transferFile(String fid, UserDto userDto);

	/**
	 * 获取文件路径
	 * @return:文件路径
	 */
	String getFile(String fid,String uid);

	/**
	 * 创建目录
	 * @return 返回创建的目录对象
	 */
	FileInfo createNewFolder(String filePid,String uid,String folderName);

	/**
	 * 根据路径id获取该目录下的所有文件夹
	 */
	List<FileInfo> getFolderInfo(String path,String userId);

	/**
	 *文件重命名
	 * @return：重命名的文件对象
	 */
	FileInfo rename(String fileId, String fileName);

	/**
	 * 移动多个文件到其他文件夹
	 * @param fileIDs
	 * @param pid
	 */
	void changeFilesPid(String fileIDs, String pid);
}