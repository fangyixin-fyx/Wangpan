package com.wangpan.controller;

import com.wangpan.annotations.GlobalInterceptor;
import com.wangpan.config.BaseConfig;
import com.wangpan.constants.Constants;
import com.wangpan.dto.UploadResultDto;
import com.wangpan.dto.UserDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.query.FileQuery;
import com.wangpan.entity.vo.FileVO;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.enums.FileCategoryEnum;
import com.wangpan.enums.FileDelFlagEnum;
import com.wangpan.enums.FileStatusEnum;
import com.wangpan.service.FileService;
import com.wangpan.utils.StringTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


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
	public ResponseVO uploadFile(HttpSession session, String fileId, MultipartFile file,String fileName,
								 String filePid,String fileMd5,Integer chunkIndex,Integer chunks){
		UserDto userDto=getUserInfoFromSession(session);
		UploadResultDto resultDto=fileService.uploadFile(userDto,fileId,file,fileName,filePid,fileMd5,chunkIndex,chunks);
		return getSuccessResponseVO(resultDto);
	}

	/**
	 * 通过图片名获取图片，显示缩略图或封面
	 */
	@GetMapping("getImage/{imageName}")
	@GlobalInterceptor(checkParam = true)
	public void getImage(HttpServletResponse response, @PathVariable("imageName") String imageName){
		if(StringTool.isEmpty(imageName)){
			return;
		}
		String imageSuffix=fileService.getFileSuffix(imageName);
		String baseFilePath=baseConfig.getProjectFolder()+"/"+Constants.FILE_PATH;
		String filePath=fileService.findFilePath(baseFilePath,imageName);
		imageSuffix=imageSuffix.replace(".","");
		String contentType="image/"+imageSuffix;
		response.setContentType(contentType);
		response.setHeader("Cache-Control","max-age=2592000");
		readFile(response,filePath);
	}

}