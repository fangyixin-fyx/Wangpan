package com.wangpan.service.impl;

import com.wangpan.constants.Constants;
import com.wangpan.dto.FileShareDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.po.FileShare;
import com.wangpan.entity.query.FileShareQuery;
import com.wangpan.entity.query.SimplePage;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.enums.PageSize;
import com.wangpan.enums.ShareValidTypeEnum;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.FileMapper;
import com.wangpan.mapper.FileShareMapper;
import com.wangpan.service.FileShareService;
import com.wangpan.utils.DateUtil;
import com.wangpan.utils.StringTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("fileShareService")
public class FileShareServiceImpl implements FileShareService {
	@Resource
	private FileShareMapper<FileShare,FileShareQuery> fileShareMapper;
	@Autowired
	private FileMapper fileMapper;


	/** 
	 * 根据条件查询列表
	 */
	public List<FileShare> findListByParam(FileShareQuery query){
		return fileShareMapper.selectList(query);
	}

	/** 
	 * 根据条件查询数量
	 */
	public Integer  findCountByParam(FileShareQuery query){
		return fileShareMapper.selectCount(query);
	}

	/** 
	 * 分页查询
	 */
	public PaginationResultVO<FileShare> findListByPage(FileShareQuery query){
		int count=Math.toIntExact(findCountByParam(query));
		int pageSize= query.getPageSize()==null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<FileShareDto> list=fileShareMapper.selectListByUid(query);
		PaginationResultVO<FileShare> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/** 
	 * 新增
	 */
	public Integer add(FileShare bean){
		return fileShareMapper.insert(bean);
	}

	/** 
	 * 批量新增
	 */
	public Integer addBatch(List<FileShare> beanList){
		if(beanList==null || beanList.isEmpty()) { return 0; }
		return fileShareMapper.insertBatch(beanList);
	}

	/** 
	 * 批量修改
	 */
	public Integer addOrUpdateBatch(List<FileShare> beanList){
		if(beanList==null || beanList.isEmpty()) { return 0; }
		return fileShareMapper.insertOrUpdateBatch(beanList);
	}

	/** 
	 * 根据 ShareId 查询
	 */
	public FileShare getFileShareByShareId(String shareId){
		return fileShareMapper.selectByShareId(shareId);
	}


	/** 
	 * 根据 ShareId 更新
	 */
	public Integer updateFileShareByShareId( FileShare bean, String shareId){
		return fileShareMapper.updateByShareId(bean,shareId);
	}


	/** 
	 * 根据 ShareId 删除
	 */
	public Integer deleteFileShareByShareId(String shareId){
		return fileShareMapper.deleteByShareId(shareId);
	}

	/**
	 * 分享文件
	 */
	public FileShare shareFile(FileShareQuery fileShareQuery){
		FileInfo fileInfo=fileMapper.selectByFid(fileShareQuery.getFileId());
		if(fileInfo==null){
			throw new BusinessException("分享文件不存在");
		}
		if(fileShareQuery.getCodeType()==1){
			fileShareQuery.setCode(StringTool.getRandomNumber(Constants.LENGTH_5));

		}
		Date currTime=new Date();
		FileShare fileShare=new FileShare();
		fileShare.setFileId(fileInfo.getFid());
		//shareId随机生成
		fileShare.setShareId(StringTool.getRandomNumber(Constants.SHAREID_LENGTH));
		fileShare.setUserId(fileShareQuery.getUserId());
		fileShare.setShareTime(currTime);
		fileShare.setCode(fileShareQuery.getCode());
		fileShare.setValidType(fileShareQuery.getValidType());
		if(!fileShareQuery.getValidType().equals(ShareValidTypeEnum.FOREVER.getType())){
			fileShare.setExpireTime(DateUtil.getAfterDay(ShareValidTypeEnum.getByType(fileShareQuery.getValidType()).getDay()));
		}else {

		}
		fileShare.setShowCount(0);
		int result=fileShareMapper.insert(fileShare);
		if(result<1){
			throw new BusinessException("文件分享失败！数据库操作有误");
		}
		return fileShare;
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteFileShareBatch(String shareIds){
		String[] idArray=shareIds.split(",");
		int result=fileShareMapper.deleteBatchByShareId(idArray);
		if(result!=idArray.length){
			//批量删除失败就回滚
			throw new BusinessException("数据库删除分享文件失败");
		}
	}
}