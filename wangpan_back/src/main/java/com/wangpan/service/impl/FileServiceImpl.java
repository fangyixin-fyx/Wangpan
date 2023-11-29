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
import com.wangpan.utils.DateUtil;
import com.wangpan.utils.StringTool;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
	@Autowired
	@Lazy   //避免循环依赖
	private FileServiceImpl fileService;


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
		boolean uploadSuccess=true;
		File tempFileFold =null;
		String uid=userDto.getUid();

		try{
			//第一个分片没有fileId
			if(StringTool.isEmpty(fileId)){
				fileId= StringTool.getRandomNumber(Constants.LENGTH_10);
			}
			resultDto.setFileId(fileId);
			Date currentDate=new Date();
			//获取当前空间信息
			UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(uid);

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
					file0.setUserId(uid);
					file0.setCreateTime(currentDate);
					file0.setLastUpdateTime(currentDate);
					file0.setStatus(FileStatusEnum.SUCCESS.getStatus());
					file0.setDelFlag(FileDelFlagEnum.USING.getStatus());
					file0.setFileMd5(fileMd5);
					//文件重命名，防止同名冲突
					if (fileNameIsExist(filePid, uid, fileName)) {
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
			String key = Constants.REDIS_USER_FILE_TEMP_SIZE + uid + fileId;
			Long currentTempSize = redisComponent.getFileSizeFromRedis(key);
			if (currentTempSize + file.getSize() > userSpaceDto.getTotalSpace()) {
				throw new BusinessException(ResponseCodeEnum.CODE_FILE);
			}

			//暂存临时目录temp
			String tempFoldName = baseConfig.getProjectFolder() + Constants.FOLD_TEMP;
			String currentUserFolder = tempFoldName + userDto.getUid() + fileId;
			tempFileFold = new File(currentUserFolder);
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

			//最后一个分片进行异步合并分片
			String month= DateUtil.format(new Date(),DateTimePatternEnum.YYYY_MM.getPattern());
			String suffix= getFileSuffix(fileName);
			String realFileName=currentUserFolder+suffix;
			FileTypeEnum fileTypeEnum=FileTypeEnum.getFileTypeBySuffix(suffix);
			//重命名
			if(fileNameIsExist(filePid,userDto.getUid(),fileName)){
				fileName=getFileNameNoSuffix(fileName) + "_" + currentDate + getFileSuffix(fileName);
			}
			//插入数据库
			FileInfo fileInfo=new FileInfo();
			fileInfo.setFid(fileId);
			fileInfo.setUserId(uid);
			fileInfo.setFileMd5(fileMd5);
			fileInfo.setFileName(fileName);
			fileInfo.setFilePath(month+"/"+realFileName);
			fileInfo.setFilePid(filePid);
			fileInfo.setCreateTime(currentDate);
			fileInfo.setLastUpdateTime(currentDate);
			fileInfo.setFileCategory(fileTypeEnum.getCategory().getCategory());
			fileInfo.setFileType(fileTypeEnum.getType());
			fileInfo.setStatus(FileStatusEnum.TRANSFER.getStatus());
			fileInfo.setFolderType(FileFolderTypeEnum.FILE.getType());
			fileInfo.setDelFlag(FileDelFlagEnum.USING.getStatus());
			fileMapper.insert(fileInfo);

			//更新redis的用户空间数据
			redisComponent.setFileTempSize(uid,fileId,file.getSize());
			Long totalSize=redisComponent.getFileSizeFromRedis(Constants.REDIS_USER_FILE_TEMP_SIZE + uid + fileId);
			updateUserSpace(userDto,totalSize);

			resultDto.setStatus(UploadStatusEnum.UPLOADED.getCode());

			//事务提交后再进行转码，即整个事务提交后再调用transferFile()
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					fileService.transferFile(fileInfo.getFid(),userDto); //这样调用异步管理才能生效
				}
			});

			return resultDto;

		}catch (BusinessException e){
			logger.error("文件上传失败！",e);
			uploadSuccess=false;
			throw e;
		}catch (Exception e){
			logger.error("文件上传失败！",e);
			uploadSuccess=false;
		}finally {
			if(!uploadSuccess && tempFileFold!=null){
				//删除临时目录
				try{
					FileUtils.deleteDirectory(tempFileFold);
				}catch (Exception e){
					logger.error("上传失败，删除临时目录",e);
				}
			}
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

	/**
	 * 文件转码，异步实现
	 * @param fileId
	 * @param userDto
	 */
	@Async
	public void transferFile(String fileId,UserDto userDto){
		Boolean transferSuccess=true;
		String targetFilePath=null;
		String cover=null;
		FileTypeEnum fileTypeEnum=null;
		FileInfo fileInfo=fileMapper.selectByFidAndUserId(fileId,userDto.getUid());
		if(fileInfo==null || fileInfo.getStatus().equals(FileStatusEnum.TRANSFER)){
			return;
		}
		try{
			//找到临时目录
			String tempFileName=baseConfig.getProjectFolder()+"/"+Constants.FOLD_TEMP;
			String currentUserFolderName=userDto.getUid()+fileId;
			File fileFolder=new File(tempFileName+currentUserFolderName);

			String fileSuffix=getFileSuffix(fileInfo.getFileName());
			String month=DateUtil.format(fileInfo.getCreateTime(),DateTimePatternEnum.YYYY_MM.getPattern());

			//目标目录
			String targetFolderBasePath=baseConfig.getProjectFolder()+Constants.FILE_PATH;
			File targetFolder=new File(targetFolderBasePath+"/"+month);
			if(!targetFolder.exists()){
				targetFolder.mkdirs();
			}
			//真实存储的文件名
			String fileName=currentUserFolderName+fileSuffix;
			targetFilePath=targetFolder.getPath()+"/"+fileName;

			//合并文件
			unionFiles(fileFolder.getPath(),targetFilePath,fileInfo.getFileName(),true);

			//视频文件切割

		}catch (Exception e){
			logger.error("文件转码失败，文件ID:{}，userid:{}",fileId,userDto.getUid(),e);
			transferSuccess=false;
		}finally {
			FileInfo updateFile=new FileInfo();
			updateFile.setFileSize(new File(targetFilePath).length());
			updateFile.setFileCover(cover);
			updateFile.setStatus(transferSuccess ? FileStatusEnum.SUCCESS.getStatus() : FileStatusEnum.FAIL.getStatus());
			fileMapper.updateStatusWithOldStatus(fileId,userDto.getUid(),updateFile,FileStatusEnum.TRANSFER.getStatus());
		}
	}

	private void unionFiles(String dirPath,String toFilePath,String fileName,Boolean isDel){
		File dir=new File(dirPath);
		if(!dir.exists()){
			throw new BusinessException("temp目录不存在");
		}
		File[] files=dir.listFiles();
		File targetFile =new File(toFilePath);	//目标文件
		RandomAccessFile writeFile=null;
		try{
			writeFile=new RandomAccessFile(targetFile,"rw");  //读写
			//一次读的数据大小
			byte[] bytes=new byte[1024*10];
			//依次读取分片文件
			for(int i=0;i<files.length;i++){
				int len=-1;
				File chunkFile=new File(dirPath+"/"+i);
				RandomAccessFile readFile=null;
				try {
					readFile=new RandomAccessFile(chunkFile,"r");
					while((len=readFile.read(bytes))!=-1){
						//将读取数据写入文件
						writeFile.write(bytes,0,len);
					}
				}catch (Exception e){
					logger.error("读取分片文件失败",e);
				}finally {
					readFile.close();
				}
			}
		}catch (Exception e){
			logger.error("合并文件:{} 失败",fileName,e);
			throw new BusinessException("合并文件"+fileName+"失败");
		}finally {
			//关闭IO
			if(writeFile!=null){
				try {
					writeFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//删除临时文件
			if(isDel&&dir.exists()){
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}