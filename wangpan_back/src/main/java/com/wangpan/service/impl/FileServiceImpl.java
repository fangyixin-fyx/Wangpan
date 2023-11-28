package com.wangpan.service.impl;

import com.wangpan.config.BaseConfig;
import com.wangpan.config.RedisComponent;
import com.wangpan.constants.Constants;
import com.wangpan.dto.UploadResultDto;
import com.wangpan.dto.UserDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.query.FileQuery;
import com.wangpan.entity.query.SimplePage;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.enums.*;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.FileMapper;
import com.wangpan.mapper.UserMapper;
import com.wangpan.service.FileService;
import com.wangpan.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

@Service("fileService")
public class FileServiceImpl implements FileService {
	@Resource
	private FileMapper<FileInfo,FileQuery> fileMapper;
	@Autowired
	private RedisComponent redisComponent;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private BaseConfig baseConfig;

	private static final Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

	/** 
	 * 根据条件查询列表
	 */
	public List<FileInfo> findListByParam(FileQuery query){
		return fileMapper.selectList(query);
	}

	/** 
	 * 根据条件查询数量
	 */
	public Integer  findCountByParam(FileQuery query){
		return fileMapper.selectCount(query);
	}

	/** 
	 * 分页查询
	 */
	public PaginationResultVO<FileInfo> findListByPage(FileQuery query){
		int count=Math.toIntExact(this.findCountByParam(query));
		int pageSize= query.getPageSize()==null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<FileInfo> list=this.findListByParam(query);
		PaginationResultVO<FileInfo> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/** 
	 * 新增
	 */
	public Integer add(FileInfo bean){
		return fileMapper.insert(bean);
	}

	/** 
	 * 批量新增
	 */
	public Integer addBatch(List<FileInfo> beanList){
		if(beanList==null || beanList.isEmpty()) { return 0; }
		return fileMapper.insertBatch(beanList);
	}

	/** 
	 * 批量修改
	 */
	public Integer addOrUpdateBatch(List<FileInfo> beanList){
		if(beanList==null || beanList.isEmpty()) { return 0; }
		return fileMapper.insertOrUpdateBatch(beanList);
	}

	/** 
	 * 根据 FidAndUserId 查询
	 */
	public FileInfo getFileByFidAndUserId(String fid, String userId){
		return fileMapper.selectByFidAndUserId(fid, userId);
	}


	/** 
	 * 根据 FidAndUserId 更新
	 */
	public Integer updateFileByFidAndUserId(FileInfo bean, String fid, String userId){
		return fileMapper.updateByFidAndUserId(bean,fid, userId);
	}


	/** 
	 * 根据 FidAndUserId 删除
	 */
	public Integer deleteFileByFidAndUserId(String fid, String userId){
		return fileMapper.deleteByFidAndUserId(fid, userId);
	}

	@Transactional(rollbackFor = Exception.class)
	public UploadResultDto uploadFile(UserDto userDto, String fileId, MultipartFile file, String fileName,
									  String filePid, String fileMd5, Integer chunkIndex, Integer chunks){

		UploadResultDto resultDto=new UploadResultDto();
		try{
			//第一个分片没有fileId
			if(StringUtils.isEmpty(fileId)){
				fileId= StringUtils.getRandomNumber(Constants.LENGTH_10);
			}
			resultDto.setFileId(fileId);
			Date currentDate=new Date();
			//获取当前空间信息
			UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(userDto.getUid());

			//第一个分片
			if(chunkIndex==0) {
				FileQuery fileQuery = new FileQuery();
				fileQuery.setFileMd5(fileMd5);
				fileQuery.setSimplePage(new SimplePage(0, 1));
				fileQuery.setStatus(FileStatusEnum.TRANSFER.getStatus());
				//如果数据库已经有这个文件，直接秒传
				List<FileInfo> dbFileList = fileMapper.selectList(fileQuery);
				if (!dbFileList.isEmpty()) {
					FileInfo file0 = dbFileList.get(0);
					//判断空间大小
					if (file0.getFileSize() + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
						throw new BusinessException(ResponseCodeEnum.CODE_FILE);
					}
					//将文件复制给用户
					file0.setFid(fileId);
					file0.setFilePid(filePid);
					file0.setUserId(userDto.getUid());
					file0.setCreateTime(currentDate);
					file0.setLastUpdateTime(currentDate);
					file0.setStatus(FileStatusEnum.SUCCESS.getStatus());
					file0.setDelFlag(FileDelFlagEnum.USING.getStatus());
					file0.setFileMd5(fileMd5);
					//文件重命名，防止同名冲突
					if (fileNameIsExist(filePid, userDto.getUid(), fileName)) {
						fileName = getFileNameNoSuffix(fileName) + "_" + currentDate + getFileSuffix(fileName);
					}
					file0.setFileName(fileName);
					//更新用户使用空间
					updateUserSpace(userDto, file0.getFileSize());
					//更新文件表信息
					fileMapper.updateByFidAndUserId(file0, fileId, file0.getUserId());

					resultDto.setStatus(UploadStatusEnum.UPLOAD_SECOND.getCode());
					return resultDto;
				}
			}

			//判断文件使用的磁盘空间，先在缓存里计算
			String key = Constants.REDIS_USER_FILE_TEMP_SIZE + userDto.getUid() + fileId;
			Long currentTempSize = redisComponent.getFileSizeFromRedis(key);
			if (currentTempSize + file.getSize() > userSpaceDto.getTotalSpace()) {
				throw new BusinessException(ResponseCodeEnum.CODE_FILE);
			}

			//暂存临时目录
			String tempFoldName = baseConfig.getProjectFolder() + Constants.FOLD_TEMP;
			String currentUserFolder = tempFoldName + userDto.getUid() + fileId;
			File tempFileFold = new File(currentUserFolder);
			if (!tempFileFold.exists()) {
				tempFileFold.mkdirs();
			}

			//开始上传文件
			File newFile=new File(tempFileFold.getPath()+"/"+chunkIndex);
			file.transferTo(newFile);
			//不是最后一个分片
			if(chunkIndex<chunks-1){
				resultDto.setStatus(UploadStatusEnum.UPLOADING.getCode());
				//更新redis
				redisComponent.setFileTempSize(userDto.getUid(),fileId,file.getSize());
				return resultDto;
			}

		}catch (Exception e){
			logger.error("文件上传失败！",e);
		}

		return resultDto;
	}

	/**
	 * 检查是否有同名文件
	 */
	private boolean fileNameIsExist(String pid,String uid,String fileName){
		FileQuery fileQuery=new FileQuery();
		fileQuery.setUserId(uid);
		fileQuery.setFilePid(pid);
		fileQuery.setDelFlag(FileDelFlagEnum.USING.getStatus());
		fileQuery.setFileName(fileName);
		Integer count=fileMapper.selectCount(fileQuery);
		return count>0 ? true : false;
	}

	/**
	 * 获取文件名
	 * @return 无后缀的文件名
	 */
	private String getFileNameNoSuffix(String fileName){
		int index=fileName.lastIndexOf(".");
		if(index==-1) return fileName;
		fileName=fileName.substring(0,index);
		return fileName;
	}

	/**
	 *
	 * @return 文件后缀名
	 */
	private String getFileSuffix(String fileName){
		int index=fileName.lastIndexOf(".");
		if(index==-1) return "";
		return fileName.substring(index);
	}


	private void updateUserSpace(UserDto userDto,Long useSpace){
		//更新数据库数据
		int result=userMapper.updateUserSpace(userDto.getUid(),useSpace,null);
		if(result==0){
			throw new BusinessException(ResponseCodeEnum.CODE_FILE);
		}
		UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(userDto.getUid());
		//更新redis数据
		userSpaceDto.setUseSpace(userSpaceDto.getUseSpace()+useSpace);
		redisComponent.saveUserSpaceUsed(userDto.getUid(),userSpaceDto);
	}

}