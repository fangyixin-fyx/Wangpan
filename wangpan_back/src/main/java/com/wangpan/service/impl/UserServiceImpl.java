package com.wangpan.service.impl;

import com.wangpan.entity.po.User;
import com.wangpan.entity.query.SimplePage;
import com.wangpan.entity.query.UserQuery;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.enums.PageSize;
import com.wangpan.mapper.UserMapper;
import com.wangpan.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("UserService")
public class UserServiceImpl implements UserService {
	@Resource
	private UserMapper<User,UserQuery> userMapper;

	/** 
	 * 根据条件查询列表
	 */
	public List<User> findListByParam(UserQuery query){
		return userMapper.selectList(query);
	}

	/** 
	 * 根据条件查询数量
	 */
	public Integer  findCountByParam(UserQuery query){
		return userMapper.selectCount(query);
	}

	/** 
	 * 分页查询
	 */
	public PaginationResultVO<User> findListByPage(UserQuery query){
		int count=Math.toIntExact(this.findCountByParam(query));
		int pageSize= query.getPageSize()==null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<User> list=this.findListByParam(query);
		PaginationResultVO<User> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/** 
	 * 新增
	 */
	public Integer add(User bean){
		return userMapper.insert(bean);
	}

	/** 
	 * 批量新增
	 */
	public Integer addBatch(List<User> beanList){
		if(beanList==null || beanList.isEmpty()) { return 0; }
		return userMapper.insertBatch(beanList);
	}

	/** 
	 * 批量修改
	 */
	public Integer addOrUpdateBatch(List<User> beanList){
		if(beanList==null || beanList.isEmpty()) { return 0; }
		return userMapper.insertOrUpdateBatch(beanList);
	}

	/** 
	 * 根据 Uid 查询
	 */
	public User getUserByUid(String uid){
		return userMapper.selectByUid(uid);
	}


	/** 
	 * 根据 Uid 更新
	 */
	public Integer updateUserByUid( User bean, String uid){
		return userMapper.updateByUid(bean,uid);
	}


	/** 
	 * 根据 Uid 删除
	 */
	public Integer deleteUserByUid(String uid){
		return userMapper.deleteByUid(uid);
	}


	/** 
	 * 根据 Email 查询
	 */
	public User getUserByEmail(String email){
		return userMapper.selectByEmail(email);
	}


	/** 
	 * 根据 Email 更新
	 */
	public Integer updateUserByEmail( User bean, String email){
		return userMapper.updateByEmail(bean,email);
	}


	/** 
	 * 根据 Email 删除
	 */
	public Integer deleteUserByEmail(String email){
		return userMapper.deleteByEmail(email);
	}


	/** 
	 * 根据 QqOpenID 查询
	 */
	public User getUserByQqOpenID(String qqOpenID){
		return userMapper.selectByQqOpenID(qqOpenID);
	}


	/** 
	 * 根据 QqOpenID 更新
	 */
	public Integer updateUserByQqOpenID( User bean, String qqOpenID){
		return userMapper.updateByQqOpenID(bean,qqOpenID);
	}


	/** 
	 * 根据 QqOpenID 删除
	 */
	public Integer deleteUserByQqOpenID(String qqOpenID){
		return userMapper.deleteByQqOpenID(qqOpenID);
	}


	/** 
	 * 根据 Username 查询
	 */
	public User getUserByUsername(String username){
		return userMapper.selectByUsername(username);
	}


	/** 
	 * 根据 Username 更新
	 */
	public Integer updateUserByUsername( User bean, String username){
		return userMapper.updateByUsername(bean,username);
	}


	/** 
	 * 根据 Username 删除
	 */
	public Integer deleteUserByUsername(String username){
		return userMapper.deleteByUsername(username);
	}


}