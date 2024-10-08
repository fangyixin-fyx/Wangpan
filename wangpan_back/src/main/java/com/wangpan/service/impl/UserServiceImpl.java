package com.wangpan.service.impl;

import com.wangpan.config.BaseConfig;
import com.wangpan.constants.Constants;
import com.wangpan.dto.UserDto;
import com.wangpan.dto.UserForAdminDto;
import com.wangpan.dto.UserSpaceDto;
import com.wangpan.entity.po.User;
import com.wangpan.entity.query.SimplePage;
import com.wangpan.entity.query.UserQuery;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.enums.PageSize;
import com.wangpan.enums.ResponseCodeEnum;
import com.wangpan.enums.UserStateEnum;
import com.wangpan.exception.BusinessException;
import com.wangpan.mapper.FileMapper;
import com.wangpan.mapper.UserMapper;
import com.wangpan.service.EmailCodeService;
import com.wangpan.service.UserService;
import com.wangpan.utils.RedisUtils;
import com.wangpan.utils.StringTool;
import org.apache.commons.lang3.ArrayUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service("userService")
public class UserServiceImpl implements UserService {
	@Autowired
	private UserMapper<User,UserQuery> userMapper;

	@Autowired
	private EmailCodeService emailCodeService;
	@Autowired
	private BaseConfig baseConfig;
	@Autowired
	private FileMapper fileMapper;
	@Autowired
	private RedisUtils redisUtils;

	private Logger logger= LoggerFactory.getLogger(UserServiceImpl.class);

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
		//获取锁
		Config config=new Config();
		config.useSingleServer()
				.setAddress("redis://"+baseConfig.getRedis_host()+":"+baseConfig.getRedis_port())
				.setPassword(baseConfig.getRedis_psw());
		RedissonClient redisson= Redisson.create(config);
		RLock lock=redisson.getLock(Constants.REDISSON_LOCK_KEY+email);
		try {
			//获取锁
			lock.lock(Constants.LOCK_EXPIRE_TIME,TimeUnit.SECONDS);

			//注册逻辑
			//邮箱是否注册已在获取邮箱验证码时验证
			//User userByEmail=userMapper.selectByEmail(email);
			//if(userByEmail!=null) throw new BusinessException("邮箱已被注册");
			User userByName=userMapper.selectByUsername(username);
			if(userByName!=null) throw new BusinessException("用户名已被使用");

			//校验邮箱验证码
			emailCodeService.checkCode(email,emailCode);
			//有可能会重复，需要改进
			String userID= StringTool.getRandomNumber(Constants.LENGTH_10);

			//user信息
			User newUser=new User();
			newUser.setUid(userID);
			newUser.setUsername(username);
			newUser.setPassword(StringTool.encodeByMD5(password)); //对密码加密
			newUser.setEmail(email);
			newUser.setRegistrationTime(new Date());
			newUser.setState(UserStateEnum.ABLE.getState());
			newUser.setUseSpace(0L);
			//系统配置初始内存参数
			newUser.setTotalSpace(Constants.userInitUseSpace*Constants.MB);
			userMapper.insert(newUser);
		} finally {
			lock.unlock();
			redisson.shutdown();
		}
	}

	public UserDto login(String email, String password){
		User user=userMapper.selectByEmail(email);
		if(user==null){
			throw new BusinessException("用户不存在！");
		}
		if(!password.equals(user.getPassword())){
			throw new BusinessException("账号或密码错误！");
		}
		if(UserStateEnum.DISABLE.getState().equals(user.getState())){
			throw new BusinessException("用户已被禁用");
		}
		//更新最新登录时间
		user.setLastLoginTime(new Date());
		userMapper.updateByUid(user,user.getUid());

		UserDto userDto =new UserDto();
		userDto.setUid(user.getUid());
		userDto.setUsername(user.getUsername());
		if(ArrayUtils.contains(baseConfig.getAdminEmails().split(","),email)){
			userDto.setIsAdmin(true);
		}else{
			userDto.setIsAdmin(false);
		}
		//用户空间
		//UserSpaceDto是否可改进？应该不需要额外定义
		UserSpaceDto userSpaceDto=new UserSpaceDto();
		userSpaceDto.setTotalSpace(user.getTotalSpace());
		//Long useSpace=fileMapper.getUsedSpaceByUid(user.getUid());
		//userSpaceDto.setUseSpace(useSpace);
		userSpaceDto.setUseSpace(user.getUseSpace());
		String redisKey=Constants.REDIS_KEY_USERSPACE_USED+user.getUid();
		redisUtils.setByTime(redisKey,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
		return userDto;
	}

	@Transactional(rollbackFor = Exception.class)
	public void resetPwd(String email,String password,String emailCode){
		User user=userMapper.selectByEmail(email);
		if(user==null) throw new BusinessException("用户不存在！");
		emailCodeService.checkCode(email,emailCode);
		user.setPassword(StringTool.encodeByMD5(password));
		userMapper.updateByEmail(user,email);
	}

	/**
	 * 管理员查看用户信息
	 */
	public PaginationResultVO<UserForAdminDto> adminGetUserListByPage(UserQuery query){
		int count=Math.toIntExact(this.findCountByParam(query));
		int pageSize= query.getPageSize()==null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		//List<User> list=this.findListByParam(query);
		List<UserForAdminDto> list=userMapper.adminSelectList(query);
		PaginationResultVO<UserForAdminDto> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	public int updateUserSpace(String userId,String changeSpace,String currUser){
		if(changeSpace==null) return 0;
		Long totalSpace=Long.valueOf(changeSpace);
		Long space=totalSpace*Constants.MB;
		User user=userMapper.selectByUid(userId);
		if(user==null) throw new BusinessException("用户不存在");
		if(space<user.getUseSpace()){
			throw new BusinessException("修改失败，使用空间大于修改空间！");
		}
		int result=userMapper.updateUserSpace2(userId,null, space);
		//如果是当前用户修改自身，需要更新redis数据
		if(result>0&&userId.equals(currUser)){
			UserSpaceDto userSpaceDto=new UserSpaceDto();
			userSpaceDto.setUseSpace(user.getUseSpace());
			userSpaceDto.setTotalSpace(space);
			//redisComponet.saveUserSpaceUsed(userId,userSpaceDto);
			redisUtils.setByTime(Constants.REDIS_KEY_USERSPACE_USED+userId,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
		}
		return result;
	}

	public int updateStatusByAdmin(String userId, String status, String currUid){
		User user=new User();
		user.setState(Integer.valueOf(status));
		int result=userMapper.updateByUid(user,userId);
		if(result>0&&userId.equals(currUid) && status.equals(String.valueOf(UserStateEnum.DISABLE.getState()))){
			return -1;
		}else{
			return result;
		}
	}

	public String getAvatar(String userId){
		String path=baseConfig.getProjectFolder()+Constants.AVATAR_PATH;
		File pathFile=new File(path);
		//判断路径是否存在
		if(!pathFile.exists()) pathFile.mkdirs();
		String avatarFilePath=path+userId+".jpg"; //绝对路径
		File avatarFile=new File(avatarFilePath);
		//如果头像不存在，赋予默认头像
		if(!avatarFile.exists()){
			String defaultAvatarFilePath=path+Constants.AVATAR_DEFAULT;
			//如果默认头像不存在
			if(!new File(defaultAvatarFilePath).exists()){
				logger.error("业务异常，服务器未存储默认头像数据");
				throw new BusinessException("默认头像不存在，系统异常。");
			}
			avatarFilePath=defaultAvatarFilePath;
		}
		return avatarFilePath;
	}

	public void logout(HttpSession session){
		//删除redis内的用户空间数据
		String uid=((UserDto) session.getAttribute(Constants.SESSION_USER)).getUid();
		redisUtils.delete(Constants.REDIS_KEY_USERSPACE_USED+uid);
		//删除redis内的系统邮件默认值
		redisUtils.delete(Constants.REDIS_KEY_SYS_SETTINGS);
		//删除保存用户信息的session
		session.invalidate();
	}


}