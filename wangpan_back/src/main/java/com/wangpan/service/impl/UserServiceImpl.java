package com.wangpan.service.impl;

import com.wangpan.config.BaseConfig;
import com.wangpan.config.RedisConfig;
import com.wangpan.constants.Constants;
import com.wangpan.dto.SysSettingsDto;
import com.wangpan.dto.UserDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.entity.po.User;
import com.wangpan.entity.query.SimplePage;
import com.wangpan.entity.query.UserQuery;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.enums.PageSize;
import com.wangpan.enums.UserStateEnum;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.UserMapper;
import com.wangpan.service.EmailCodeService;
import com.wangpan.service.UserService;
import com.wangpan.utils.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("UserService")
public class UserServiceImpl implements UserService {
	@Autowired
	private UserMapper<User,UserQuery> userMapper;

	@Autowired
	private EmailCodeService emailCodeService;
	@Autowired
	private RedisConfig redisConfig;
	@Autowired
	private BaseConfig baseConfig;

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

	/**
	 * 注册
	 * 事务处理：插入失败，但是验证码还能有效
	 */
	@Transactional(rollbackFor = Exception.class)
	public void register(String username,String password,String email,String emailCode){
		User userByEmail=userMapper.selectByEmail(email);
		if(userByEmail!=null) throw new BusinessException("邮箱已被注册");
		User userByName=userMapper.selectByUsername(username);
		if(userByName!=null) throw new BusinessException("用户名已被使用");
		//校验邮箱验证码
		emailCodeService.checkCode(email,emailCode);

		String userID= StringUtil.getRandomNumber(Constants.LENGTH_10);

		//user信息
		User newUser=new User();
		newUser.setUid(userID);
		newUser.setUsername(username);
		newUser.setPassword(StringUtil.encodeByMD5(password)); //对密码加密
		newUser.setEmail(email);
		newUser.setRegistrationTime(new Date());
		newUser.setState(UserStateEnum.ABLE.getState());
		newUser.setUseSpace(0L);
		//系统配置初始内存参数
		SysSettingsDto sysSettingsDto=redisConfig.getSysSettingDto();
		newUser.setTotalSpace(sysSettingsDto.getUserInitUseSpace()*Constants.MB);
		userMapper.insert(newUser);

	}

	public UserDto login(String email,String password){
		User user=userMapper.selectByEmail(email);
		if(!password.equals(user.getPassword())){
			throw new BusinessException("账号或密码错误！");
		}
		if(UserStateEnum.DISABLE.getState().equals(user.getState())){
			throw new BusinessException("用户已被禁用");
		}
		//更新最新登录时间
		user.setLastLoginTime(new Date());
		userMapper.updateByUid(user,user.getUid());

		UserDto userDto=new UserDto();
		userDto.setUid(user.getUid());
		userDto.setUsername(user.getUsername());
		if(ArrayUtils.contains(baseConfig.getAdminEmails().split(","),email)){
			userDto.setIsAdmin(true);
		}else{
			userDto.setIsAdmin(false);
		}
		//用户空间
		UserSpaceDto userSpaceDto=new UserSpaceDto();
		userSpaceDto.setTotalSpace(userSpaceDto.getTotalSpace());
		redisConfig.saveUserSpaceUsed(user.getUid(),userSpaceDto);	//将用户使用空间存入redis，可修改

		return userDto;
	}


}