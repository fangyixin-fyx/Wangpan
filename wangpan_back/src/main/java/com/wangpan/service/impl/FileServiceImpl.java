package com.wangpan.service.impl;

import com.wangpan.config.BaseConfig;
import com.wangpan.config.RedisComponent;
import com.wangpan.constants.Constants;
import com.wangpan.dto.DownloadFileDto;
import com.wangpan.dto.UploadResultDto;
import com.wangpan.dto.UserDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.po.User;
import com.wangpan.entity.query.FileQuery;
import com.wangpan.entity.query.SimplePage;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.enums.*;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.FileMapper;
import com.wangpan.mapper.UserMapper;
import com.wangpan.service.FileService;
import com.wangpan.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
	@Resource
	@Lazy   //避免循环依赖
	private FileServiceImpl fileService;
	@Autowired
	private RedisUtils redisUtils;


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
			//UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(uid);
			UserSpaceDto userSpaceDto=(UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USERSPACE_USED+uid);
			//第一个分片
			if(chunkIndex==0) {
				//如果数据库已经有这个文件，直接秒传
				List<FileInfo> dbFileList = fileMapper.selectByFileMd5(fileMd5);
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
					//文件重命名，防止同名冲突
					if (fileNameIsExist(filePid, uid, fileName)) {
						String timePattern=DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern();
						fileName = getFileNameNoSuffix(fileName) + "_" + DateUtil.format(currentDate,timePattern) +
								getFileSuffix(fileName);
					}
					file0.setFileName(fileName);
					//更新用户使用空间
					updateUserSpace(userDto, file0.getFileSize());
					//更新文件表信息
					//fileMapper.updateByFidAndUserId(file0,dbFileList.get(0).getFid(),dbFileList.get(0).getUserId());
					fileMapper.insert(file0);

					resultDto.setStatus(UploadStatusEnum.UPLOAD_SECOND.getCode());
					return resultDto;
				}
			}

			//判断文件使用的磁盘空间，先在缓存里计算
			String sizeKey = Constants.REDIS_USER_FILE_TEMP_SIZE + uid + fileId;
			Long currentTempSize = redisUtils.getFileSizeFromRedis(sizeKey);
			if (currentTempSize + file.getSize() > userSpaceDto.getTotalSpace()) {
				throw new BusinessException(ResponseCodeEnum.CODE_FILE);
			}

			//找到暂存临时目录temp
			String tempFoldName = baseConfig.getProjectFolder() + Constants.FOLD_TEMP;
			//String currentUserFolder = tempFoldName + uid + fileId;
			String currentUserFolder = tempFoldName + uid + fileMd5;
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
				//redisComponent.setFileTempSize(uid,fileId,file.getSize());
				redisUtils.setFileTempSize(uid,fileId,file.getSize());
				return resultDto;
			}

			//最后一个分片上传完毕，记录数据库，同时异步合并分片
			String month= DateUtil.format(new Date(),DateTimePatternEnum.YYYY_MM.getPattern());
			String suffix= getFileSuffix(fileName);
			String realFileName=uid+fileMd5+suffix;
			FileTypeEnum fileTypeEnum=FileTypeEnum.getFileTypeBySuffix(suffix);
			//重命名
			if(fileNameIsExist(filePid,userDto.getUid(),fileName)){
				fileName=getFileNameNoSuffix(fileName) + "_" + currentDate + getFileSuffix(fileName);
			}

			//更新redis的用户空间数据
			//redisComponent.setFileTempSize(uid,fileId,file.getSize());
			redisUtils.setFileTempSize(uid,fileId,file.getSize());
			//获取文件的所有分片空间使用大小
			//Long totalSize=redisComponent.getFileSizeFromRedis(Constants.REDIS_USER_FILE_TEMP_SIZE + uid + fileId);
			Long totalSize=redisUtils.getFileSizeFromRedis(Constants.REDIS_USER_FILE_TEMP_SIZE + uid + fileId);

			//插入数据库
			FileInfo fileInfo=new FileInfo();
			fileInfo.setFid(fileId);
			fileInfo.setUserId(uid);
			fileInfo.setFileMd5(fileMd5);
			fileInfo.setFileName(fileName);
			fileInfo.setFilePath(uid+"/"+month+"/"+realFileName);
			fileInfo.setFilePid(filePid);
			fileInfo.setCreateTime(currentDate);
			fileInfo.setLastUpdateTime(currentDate);
			fileInfo.setFileCategory(fileTypeEnum.getCategory().getCategory());
			fileInfo.setFileType(fileTypeEnum.getType());
			fileInfo.setStatus(FileStatusEnum.TRANSFER.getStatus());
			fileInfo.setFolderType(FileFolderTypeEnum.FILE.getType());
			fileInfo.setDelFlag(FileDelFlagEnum.USING.getStatus());
			fileInfo.setFileSize(totalSize);
			//更新file表
			fileMapper.insert(fileInfo);
			//更新user表
			updateUserSpace(userDto,totalSize);

			resultDto.setStatus(UploadStatusEnum.UPLOADED.getCode());

			//事务提交后再进行异步合并分片，即整个事务提交后再调用transferFile()
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					//通过fileId去查文件，必须要等事务提交后才有fileId
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
	public String getFileSuffix(String fileName){
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
		//UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(userDto.getUid());
		String key=Constants.REDIS_KEY_USERSPACE_USED+userDto.getUid();
		UserSpaceDto userSpaceDto=(UserSpaceDto) redisUtils.get(key);
		//更新redis数据
		userSpaceDto.setUseSpace(userSpaceDto.getUseSpace()+useSpace);
		//redisComponent.saveUserSpaceUsed(userDto.getUid(),userSpaceDto);
		redisUtils.setByTime(key,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
	}

	/**
	 * 文件转码，异步合并分片
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
		if(fileInfo==null || !fileInfo.getStatus().equals(FileStatusEnum.TRANSFER.getStatus())){
			return;
		}
		try{
			//找到临时目录
			String tempFileName=baseConfig.getProjectFolder()+"/"+Constants.FOLD_TEMP;
			//String currentUserFolderName=userDto.getUid()+fileId;
			String md5=fileInfo.getFileMd5();
			String currentUserFolderName=userDto.getUid()+md5;

			File fileFolder=new File(tempFileName+currentUserFolderName);

			String fileSuffix=getFileSuffix(fileInfo.getFileName());
			String month=DateUtil.format(fileInfo.getCreateTime(),DateTimePatternEnum.YYYY_MM.getPattern());

			//创建目标目录
			String targetFolderBasePath=baseConfig.getProjectFolder()+Constants.FILE_PATH+userDto.getUid();
			File targetFolder=new File(targetFolderBasePath+"/"+month);
			if(!targetFolder.exists()){
				targetFolder.mkdirs();
			}
			//真实存储的文件名
			String fileName=currentUserFolderName+fileSuffix;
			targetFilePath=targetFolder.getPath()+"/"+fileName;

			//合并文件
			unionFiles(fileFolder.getPath(),targetFilePath,fileInfo.getFileName(),true);

			fileTypeEnum=FileTypeEnum.getFileTypeBySuffix(fileSuffix);
			//视频文件切割，同时生成缩略图
			if(fileTypeEnum==FileTypeEnum.VIDEO){
				//视频文件切割，预览播放时使用切割文件进行播放
				cutVideoFile(fileId, targetFilePath);
				//生成视频缩略图
				cover=currentUserFolderName+".png";
				String coverPath=targetFolder.getPath()+"/"+cover;
				ScaleFilter.createCover4Video(new File(targetFilePath),150,new File(coverPath));
			}
			//图像缩略图生成
			else if(fileTypeEnum==FileTypeEnum.IMAGE){
				cover=fileName.replace(".","_.");
				String coverPath=targetFolder.getPath()+"/"+cover;
				File targetPicFile=new File(targetFilePath);
				File coverFile=new File(coverPath);
				Boolean success=ScaleFilter.createCover4Pic(targetPicFile,150,coverFile,false);
				if(!success){
					//图太小就把原图复制给缩略图
					FileUtils.copyFile(targetPicFile,coverFile);
				}
			}
			//删除redis存储的文件临时大小
			String sizeKey = Constants.REDIS_USER_FILE_TEMP_SIZE + userDto.getUid() + fileId;
			redisUtils.delete(sizeKey);

		}catch (Exception e){
			logger.error("文件转码失败，文件ID:{}，userid:{}",fileId,userDto.getUid(),e);
			transferSuccess=false;
		}finally {
			FileInfo updateFile=new FileInfo();
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
		File targetFile =new File(toFilePath);	//创建目标文件
		BufferedOutputStream writeFile=null;
		try{
			writeFile=new BufferedOutputStream(new FileOutputStream(targetFile));  //读写
			//一次读的数据大小
			byte[] bytes=new byte[1024*10];
			//依次读取分片文件
			for(int i=0;i<files.length;i++){
				int len=-1;
				File chunkFile=new File(dirPath+"/"+i);
				//RandomAccessFile readFile=null;
				BufferedInputStream readFile=null;
				try {
					//readFile=new RandomAccessFile(chunkFile,"r");
					readFile=new BufferedInputStream(new FileInputStream(chunkFile));
					//将最多 bytes.length 个数据字节从此文件读入bytes数组。
					while((len=readFile.read(bytes))!=-1){
						//将读取数据写入文件
						////从偏移量off处开始，将len个字节从bytes数组写入到此文件writeFile。
						writeFile.write(bytes,0,len);
					}
				}catch (Exception e){
					logger.error("读取分片文件失败",e);
				}finally {
					readFile.close();
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
		}
	}


	/**
	 * 视频切割方法
	 * @param videoFilePath:视频文件路径
	 */
	private void cutVideoFile(String fileId,String videoFilePath){
		//创建同名切片目录
		File tsFile=new File(videoFilePath.substring(0,videoFilePath.lastIndexOf(".")));
		if(!tsFile.exists()){
			tsFile.mkdirs();
		}
		//ffmpeg命令:先把视频文件转为ts文件然后再进行切割
		String CMD_TRANSFER_2TS=Constants.CMD_TRANSFER_2TS;
		String CMD_CUT_TS=Constants.CMD_CUT_TS;
		String tsPath= tsFile.getPath()+"/"+Constants.TS_NAME;
		//上传视频的编码格式必须是h264，否则不能转换为ts文件
		//转为ts文件,播放的时候也是切片播放的
		String cmd=String.format(CMD_TRANSFER_2TS,videoFilePath,tsPath);
		FfmpegUtils.executeCommand(cmd,false);
		//生成索引文件.m3u8和切片文件.ts
		String m3u8Path=tsFile.getPath()+"/"+Constants.M3U8_NAME;
		cmd=String.format(CMD_CUT_TS,tsPath,m3u8Path,tsFile.getPath(),fileId);
		FfmpegUtils.executeCommand(cmd,false);
		//删除转换的index.ts文件
		new File(tsPath).delete();

	}

	/**
	 * 找到特定文件，并返回全部目录
	 */
	public String findFilePath(String folderPath,String fileName){
		File folder=new File(folderPath);
		if(folder.isDirectory()){
			File[] files=folder.listFiles();
			if(files!=null){
				for(File file:files){
					if(file.isDirectory()){
						//进入子文件夹
						String filePath=findFilePath(file.getAbsolutePath(),fileName);
						if(filePath!=null) return filePath;
					}else if(file.getName().equals(fileName)){
						return file.getAbsolutePath();
					}
				}
			}
		}
		return null;
	}

	/**
	 * 分片播放视频
	 * @return 返回文件路径
	 */
	public String getFile(String fid,String uid){
		FileInfo fileInfo=null;
		if(fid.endsWith(".ts")){
			String[] tsArray=fid.split("_");
			String realFid=tsArray[0];
			fileInfo=getFileByFidAndUserId(realFid,uid);
		}else{
			fileInfo=getFileByFidAndUserId(fid,uid);
		}

		if(fileInfo==null) return null;

		String folderPath=baseConfig.getProjectFolder()+Constants.FILE_PATH;
		String filePath=null;

		//如果是视频文件，预览时不读取path值，而是同文件夹下的切片文件
		if(fileInfo.getFileCategory().equals(FileCategoryEnum.VIDEO.getCategory())){
			//String folderPath=baseConfig.getProjectFolder()+Constants.FILE_PATH+getFileNameNoSuffix(fileInfo.getFilePath());
			if(fid.endsWith(".ts")){
				filePath=folderPath+getFileNameNoSuffix(fileInfo.getFilePath())+"/"+fid;
			}else{
				//读取切片索引文件index.m3u8
				String m3u8Path=folderPath+getFileNameNoSuffix(fileInfo.getFilePath())+"/"+Constants.M3U8_NAME;
				filePath=m3u8Path;
			}

		}else{	//文档文件
			filePath=folderPath+fileInfo.getFilePath();
		}
		File file=new File(filePath);
		if(!file.exists()) return null;
		return filePath;
	}

	public FileInfo createNewFolder(String filePid,String uid,String folderName){
		//文件名重复检测
		if(checkFileName(filePid,uid,folderName,FileFolderTypeEnum.FOLDER.getType())!=0){
			throw new BusinessException("此目录下已存在同名文件，请修改名称");
		}
		FileInfo fileInfo=new FileInfo();
		Date currDate=new Date();
		fileInfo.setFilePid(filePid);
		fileInfo.setUserId(uid);
		fileInfo.setFileName(folderName);
		fileInfo.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		fileInfo.setCreateTime(currDate);
		fileInfo.setLastUpdateTime(currDate);
		fileInfo.setFid(StringTool.getRandomNumber(Constants.LENGTH_10));
		fileInfo.setStatus(FileStatusEnum.SUCCESS.getStatus());
		fileInfo.setDelFlag(FileDelFlagEnum.USING.getStatus());
		//插入数据库
		fileMapper.insert(fileInfo);
		return fileInfo;
	}

	public int checkFileName(String filePid,String uid,String name,Integer folderType){
		FileQuery fileInfo=new FileQuery();
		fileInfo.setFilePid(filePid);
		fileInfo.setFileName(name);
		fileInfo.setUserId(uid);
		fileInfo.setFolderType(folderType);
		fileInfo.setDelFlag(FileDelFlagEnum.USING.getStatus());
		int count=fileMapper.selectCount(fileInfo);
		if(count>0){
			return 1;
		}
		return 0;

	}

	public List<FileInfo> getFolderInfo(String path,String userId){
		String[] fidArray=path.split("/");
		FileQuery fileQuery=new FileQuery();
		fileQuery.setUserId(userId);
		fileQuery.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		fileQuery.setFileIdArray(fidArray);
		//根据路径fid先后进行排序，即父级在前
		String orderBy="field(fid,\""+ StringUtils.join(fidArray,"\",\"") +"\")";
		fileQuery.setOrderBy(orderBy);
		List<FileInfo> fileInfoList=new ArrayList<>();
		fileInfoList=findListByParam(fileQuery);
		return fileInfoList;
	}

	@Transactional(rollbackFor = Exception.class)
	public FileInfo rename(String fileId, String fileName){
		FileInfo fileInfo=fileMapper.selectByFid(fileId);
		if(fileInfo==null){
			throw new BusinessException("未根据fid找到相关文件");
		}
		String uid=fileInfo.getUserId();
		Integer folderType=fileInfo.getFolderType();
		if(folderType.equals(FileFolderTypeEnum.FILE.getType())){
			String suffix=getFileSuffix(fileInfo.getFileName());
			fileName=fileName+suffix;
		}
		//检查名字是否已存在
		if(checkFileName(fileInfo.getFilePid(),uid,fileName,folderType)!=0){
			throw new BusinessException("该文件名称已存在，请重新命名");
		}
		//修改名称
		fileInfo.setFileName(fileName);
		fileInfo.setLastUpdateTime(new Date());
		int res=fileMapper.updateByFidAndUserId(fileInfo,fileId,uid);
		if(res==1){
			return fileInfo;
		}else{
			throw new BusinessException("重命名失败！");
		}

	}

	@Transactional(rollbackFor = Exception.class)
	public void changeFilesPid(String fileIDs, String pid){
		if(StringTool.isEmpty(fileIDs) || StringTool.isEmpty(pid) ||
				(!pid.equals(Constants.ROOT_PID) &&fileMapper.selectByFid(pid)==null)){
			throw new BusinessException("移动文件失败，目标移动文件或目标目录不存在！");
		}

		String[] fids=fileIDs.split(",");
		FileInfo fileInfo=new FileInfo();
		Date currDate=new Date();
		//获取目标目录下的所有文件的名字
		List<String> existFileName=fileMapper.getFileNameByPid(pid);
		//改为map存储，方便查找，避免嵌套循环
		Map<String,Integer> fileNameMap=existFileName.stream().collect(Collectors.toMap(
				//使用文件名作为键
				name->name,
				//值映射为1
				name->1
		));
		//将这些文件的pid修改
		for(String fid:fids){
			fileInfo=fileMapper.selectByFid(fid);

			//判断目标移动文件与目标目录是否在同一个目录
			if(fileInfo.getFilePid().equals(pid)){
				throw new BusinessException(fileInfo.getFileName()+"文件已在该目录");
			}
			//判断移动目录是否有同名文件
			String fileName=fileInfo.getFileName();
			//if(existFileName.contains(fileName)){
			if(fileNameMap.getOrDefault(fileName,0)==1){
				fileInfo.setFileName(getFileNameNoSuffix(fileName)+"_"+
						DateUtil.format(currDate,DateTimePatternEnum.YYYY_MM_DD_HH.getPattern())+getFileSuffix(fileName));
			}
			fileInfo.setFilePid(pid);
			fileInfo.setLastUpdateTime(currDate);
			fileMapper.updateFileByFid(fileInfo,fid);
		}
	}

	public String createDownloadUrl(String fid,String uid){
		FileInfo fileInfo=fileMapper.selectByFidAndUserId(fid,uid);
		if(fileInfo==null){
			throw new BusinessException("文件不存在，生成下载链接失败");
		}
		if(fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
			throw new BusinessException("不支持下载文件夹");
		}
		//生成验证随机数
		String code=StringTool.getRandomNumber(Constants.LENGTH_15);

		DownloadFileDto downloadFileDto=new DownloadFileDto();
		downloadFileDto.setDownloadCode(code);
		downloadFileDto.setFileName(fileInfo.getFileName());
		downloadFileDto.setFilePath(fileInfo.getFilePath());
		//将下载信息存入redis中
		String key=Constants.REDIS_KEY_DOWNLOAD_CODE+code;
		redisUtils.setByTime(key,downloadFileDto,Constants.REDIS_KEY_EXPIRES_MINUTE*3L);
		return code;
	}

	public Map<String,String> download(String code){
		DownloadFileDto downloadFileDto=(DownloadFileDto) redisUtils.get(Constants.REDIS_KEY_DOWNLOAD_CODE+code);
		if(downloadFileDto==null){
			throw new BusinessException("下载验证码已失效");
		}
		String filePath=baseConfig.getProjectFolder()+Constants.FILE_PATH+downloadFileDto.getFilePath();
		String fileName=downloadFileDto.getFileName();
		Map<String,String> map=new HashMap<>();
		map.put("filePath",filePath);
		map.put("fileName",fileName);
		return map;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeFileToRecycleBin(String fids) {
		String[] fidArray=fids.split(",");
		FileInfo fileInfo=null;
		Date currTime=new Date();

		String uid=null;
		for(String fid:fidArray){
			fileInfo=fileMapper.selectByFid(fid);
			if(fileInfo==null){
				return;
			}
			uid=fileInfo.getUserId();
			//如果是文件直接移入回收站，文件夹则递归删除子文件
			if(fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
				delSubFiles(fileInfo.getFid(),uid);
			}
			//将文件移入回收站
			fileMapper.delByFid(fid,FileDelFlagEnum.RECYCLE.getStatus(),currTime);

		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delSubFiles(String pid, String uid){
		List<FileInfo> subFiles=fileMapper.selectFileByUidAndPid(uid,pid);
		if(subFiles==null) return;
		Date currTime=new Date();
		for(FileInfo subFile:subFiles){
			fileMapper.delByFid(subFile.getFid(),FileDelFlagEnum.DEL.getStatus(), currTime);
			if(subFile.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
				delSubFiles(uid,subFile.getFid());
			}
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void recoveryFile(FileQuery fileQuery){
		List<FileInfo> fileList=fileMapper.selectList(fileQuery);
		if(fileList==null || fileList.size()<1) return;
		Date currTime=new Date();
		String uid=null;
		for(FileInfo file:fileList){
			uid=file.getUserId();
			if(file.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
				//恢复子文件
				FileQuery subFileQuery=new FileQuery();
				subFileQuery.setFilePid(file.getFid());
				subFileQuery.setUserId(uid);
				subFileQuery.setDelFlag(FileDelFlagEnum.DEL.getStatus());
				recoveryFile(subFileQuery);
			}
			//如果是delFlag标记为回收站，则恢复到根目录，因为上一级目录可能已不存在
			if(file.getDelFlag().equals(FileDelFlagEnum.RECYCLE.getStatus())){
				file.setFilePid(Constants.ROOT_PID);
			}
			file.setLastUpdateTime(currTime);
			file.setDelFlag(FileDelFlagEnum.USING.getStatus());
			//检查是否有重名
			String fileName=file.getFileName();
			if(checkFileName(file.getFilePid(),uid,fileName,file.getFolderType())>0){
				String suffix=getFileSuffix(fileName);
				String pattern=DateTimePatternEnum.YYYY_MM_DD_HH.getPattern();
				file.setFileName(getFileNameNoSuffix(fileName)+"_"+DateUtil.format(currTime,pattern)+suffix);
			}
			fileMapper.updateFileByFid(file,file.getFid());
		}
		return;
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteFileCompletely(String fids,String uid){
		if(StringTool.isEmpty(fids)) return;
		String[] fidArray=fids.split(",");
		FileQuery fileQuery=new FileQuery();
		fileQuery.setFileIdArray(fidArray);
		fileQuery.setDelFlag(FileDelFlagEnum.RECYCLE.getStatus());
		//获取到文件信息
		List<FileInfo> fileList=fileMapper.selectList(fileQuery);

		//获取文件夹的子文件信息
		List<String> delIds=new ArrayList<>();
		for(FileInfo fileInfo:fileList){
			if(fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
				//获取子文件的所有ID
				deleteSubFileCompletely(fileInfo.getFid(),uid,delIds);
			}
		}
		//数据库操作
		//删除子文件
		if(delIds.size()>0){
			String[] subIds=new String[delIds.size()];
			fileMapper.deleteCompletelyByFid(delIds.toArray(subIds));
		}
		//删除父文件
		fileMapper.deleteCompletelyByFid(fidArray);
		//更新用户空间
		Long currSize=fileMapper.getUsedSpaceByUid(uid);
		String spaceKey=Constants.REDIS_KEY_USERSPACE_USED+uid;
		//UserSpaceDto userSpaceDto=redisComponent.getUsedSpaceDto(uid);
		UserSpaceDto userSpaceDto=(UserSpaceDto) redisUtils.get(spaceKey);
		userMapper.updateUserSpace2(uid,currSize,userSpaceDto.getTotalSpace());
		userSpaceDto.setUseSpace(currSize);
		//redisComponent.saveUserSpaceUsed(uid,userSpaceDto);
		redisUtils.setByTime(spaceKey,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
		//删除本地文件
		/*
		String basePath=baseConfig.getProjectFolder()+Constants.FILE_PATH;
		for(FileInfo fileInfo:fileList){
			File delFile=new File(basePath+fileInfo.getFilePath());
			try {
				FileUtils.forceDelete(delFile);
			} catch (IOException e) {
				throw new BusinessException("删除本地文件失败",e);
			}
		}

		 */
	}

	public void deleteSubFileCompletely(String pid,String uid,List<String> delIdList){
		List<FileInfo> subFiles=fileMapper.selectFileByUidAndPid(uid,pid);
		if(subFiles==null) return ;
		for(FileInfo subfile:subFiles){
			delIdList.add(subfile.getFid());
			if(subfile.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
				deleteSubFileCompletely(subfile.getFid(),subfile.getUserId(),delIdList);
			}
		}
	}

	public PaginationResultVO<FileInfo> adminFindListByPage(FileQuery query){
		int count=Math.toIntExact(this.findCountByParam(query));
		int pageSize= query.getPageSize()==null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<FileInfo> list=fileMapper.selectListByAdmin(query);
		PaginationResultVO<FileInfo> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

}