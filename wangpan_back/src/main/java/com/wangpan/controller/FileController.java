package com.wangpan.controller;

import com.wangpan.annotations.GlobalInterceptor;
import com.wangpan.config.BaseConfig;
import com.wangpan.constants.Constants;
import com.wangpan.dto.UploadResultDto;
import com.wangpan.dto.UserDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.query.FileQuery;
import com.wangpan.entity.vo.FileVO;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.enums.FileCategoryEnum;
import com.wangpan.enums.FileDelFlagEnum;
import com.wangpan.enums.FileFolderTypeEnum;
import com.wangpan.enums.FileStatusEnum;
import com.wangpan.service.FileService;
import com.wangpan.utils.CopyUtil;
import com.wangpan.utils.StringTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/file")
public class FileController extends ABaseController {
	@Autowired
	private FileService fileService;
	@Autowired
	private BaseConfig baseConfig;

	/** 
	 * 分页查询
	 */
	@RequestMapping("/loadDataList")
	@GlobalInterceptor
	public ResponseVO loadDataList(HttpSession session, String category, FileQuery query) {
		FileCategoryEnum fileCategory=FileCategoryEnum.getByCode(category);
		if(fileCategory!=null){
			query.setFileCategory(fileCategory.getCategory());
		}
		query.setUserId(getUserInfoFromSession(session).getUid());
		query.setOrderBy("lastUpdateTime desc");
		query.setDelFlag(FileDelFlagEnum.USING.getStatus());
		PaginationResultVO<FileInfo> resultVO=fileService.findListByPage(query);
		//将FileInfo转为FileInfoVO
		return getSuccessResponseVO(convert2PaginationVO(resultVO, FileVO.class));
	}

	@PostMapping("/uploadFile")
	@GlobalInterceptor
	public ResponseVO uploadFile(HttpSession session, String fileId, MultipartFile file,String fileName,
								 String filePid,String fileMd5,Integer chunkIndex,Integer chunks){
		UserDto userDto=getUserInfoFromSession(session);
		UploadResultDto resultDto=fileService.uploadFile(userDto,fileId,file,fileName,filePid,fileMd5,chunkIndex,chunks);
		return getSuccessResponseVO(resultDto);
	}

	/**
	 * 通过图片名获取图片，显示缩略图或封面
	 */
	@GetMapping("/getImage/{imageName}")
	public void getImage(HttpServletResponse response,HttpSession session, @PathVariable("imageName") String imageName){
		if(StringTool.isEmpty(imageName)){
			return;
		}
		String imageSuffix=fileService.getFileSuffix(imageName);
		String baseFilePath=baseConfig.getProjectFolder()+"/"+Constants.FILE_PATH+getUserInfoFromSession(session).getUid()+"/";
		String filePath=fileService.findFilePath(baseFilePath,imageName);
		imageSuffix=imageSuffix.replace(".","");
		response.setContentType("image/"+imageSuffix);
		response.setHeader("Cache-Control","max-age=2592000");
		readFile(response,filePath);
	}

	/**
	 * 读取视频文件接口
	 */
	@GetMapping("/ts/getVideoInfo/{fileId}")
	public void getVideoInfo(HttpServletResponse response,HttpSession session,@PathVariable("fileId") String fileId){
		UserDto userDto=getUserInfoFromSession(session);
		String filePath=fileService.getFile(fileId,userDto.getUid());
		readFile(response,filePath);
	}

	/**
	 * 预览非视频文件的接口
	 * @return: 文件流直接写入response里，不需要返回
	 */
	@PostMapping("/getFile/{fileId}")
	@GlobalInterceptor
	public void getFile(HttpServletResponse response,HttpSession session,@PathVariable("fileId") String fileId){
		UserDto userDto=getUserInfoFromSession(session);
		String filePath=fileService.getFile(fileId,userDto.getUid());
		readFile(response,filePath);
	}

	/**
	 * 新建目录
	 */
	@PostMapping("/newFoloder")
	@GlobalInterceptor
	public ResponseVO createNewFolder(HttpSession session,@RequestParam("filePid") String filePid,
								@RequestParam("fileName") String folderName){
		UserDto userDto=getUserInfoFromSession(session);
		FileInfo fileInfo=fileService.createNewFolder(filePid,userDto.getUid(),folderName);
		return getSuccessResponseVO(fileInfo);
	}

	/**
	 * 获取当前目录层级
	 */
	@PostMapping("/getFolderInfo")
	@GlobalInterceptor
	public ResponseVO getFolderInfo(HttpSession session,String path){
		String uid=getUserInfoFromSession(session).getUid();
		List<FileInfo> fileInfoList=fileService.getFolderInfo(path,uid);
		//包装类
		List<FileVO> result=CopyUtil.copyList(fileInfoList, FileVO.class);
		return getSuccessResponseVO(result);
	}

	/**
	 * 文件重命名
	 */
	@PostMapping("/rename")
	@GlobalInterceptor
	public ResponseVO rename(@RequestParam("fileId") String fileId,@RequestParam("fileName") String fileName){
		FileInfo fileInfo=fileService.rename(fileId,fileName);
		return getSuccessResponseVO(CopyUtil.copy(fileInfo, FileVO.class));
	}

	/**
	 * 获取可移动的所有目录，方便移动文件
	 * @param pid:用于获取当前层级下的所有文件
	 * @param currentFids：用于排除要移动的文件
	 * @return 可移动的所有目录对象
	 */
	@PostMapping("/loadAllFolder")
	@GlobalInterceptor
	public ResponseVO getAllFolder(HttpSession session,@RequestParam("filePid") String pid,
								   @RequestParam("currentFileIds") String currentFids){
		String uid=getUserInfoFromSession(session).getUid();
		FileQuery fileQuery=new FileQuery();
		fileQuery.setFilePid(pid);
		fileQuery.setUserId(uid);
		fileQuery.setOrderBy("createTime desc");
		if(!StringTool.isEmpty(currentFids)){
			fileQuery.setExcludeFileIdArray(currentFids.split(","));
		}
		fileQuery.setDelFlag(FileDelFlagEnum.USING.getStatus());
		fileQuery.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		List<FileInfo> result=fileService.findListByParam(fileQuery);
		return getSuccessResponseVO(CopyUtil.copyList(result, FileVO.class));
	}

	@PostMapping("/changeFileFolder")
	@GlobalInterceptor
	public ResponseVO changeFilesToOtherFolder(@RequestParam("filePid") String pid,
											   @RequestParam("fileIds") String fileIDs,
											   HttpSession session){
		String uid=getUserInfoFromSession(session).getUid();
		fileService.changeFilesPid(fileIDs,pid,uid);
		return getSuccessResponseVO(null);
	}

	@PostMapping("/createDownloadUrl/{fileId}")
	@GlobalInterceptor
	public ResponseVO createDownloadUrl(HttpSession session,@PathVariable("fileId") String fileId){
		String uid=getUserInfoFromSession(session).getUid();
		String code=fileService.createDownloadUrl(fileId,uid);
		return getSuccessResponseVO(code);
	}

	@GetMapping("/download/{code}")
	@GlobalInterceptor(checkLogin = false)
	public void download(HttpServletRequest request, HttpServletResponse response,
							   @PathVariable("code") String code) throws UnsupportedEncodingException {
		Map<String,String> map=fileService.download(code);
		String path=map.get("filePath");
		String fileName=map.get("fileName");
		response.setContentType("application/x-msdownload; charset=UTF-8");
		//如果是IE浏览器
		if(request.getHeader("User-Agent").toLowerCase().indexOf("msie")>0){
			fileName= URLEncoder.encode(fileName,"UTF-8");
		}else{
			fileName=new String(fileName.getBytes("UTF-8"),"ISO8859-1");
		}
		response.setHeader("Content-Disposition","attachment;filename=\""+fileName+"\"");
		//读取文件
		readFile(response,path);
	}

	@PostMapping("/delFile")
	@GlobalInterceptor
	public ResponseVO deleteFiles(@RequestParam("fileIds") String fids){
		fileService.removeFileToRecycleBin(fids);

		return getSuccessResponseVO(null);
	}

}