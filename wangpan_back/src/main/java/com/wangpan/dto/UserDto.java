package com.wangpan.dto;

import lombok.Data;

/**
 * 登陆后的用户信息
 * @author fangyixin
 * @date 2023/11/17 19:39
 */
@Data
public class UserDto {
    private String username;
    private String uid;
    private String avatar;  //qq头像
    private Boolean isAdmin;

}
