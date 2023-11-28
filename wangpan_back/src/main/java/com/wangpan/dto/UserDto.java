package com.wangpan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 登陆后的用户信息
 * @author fangyixin
 * @date 2023/11/17 19:39
 */
@Data
public class UserDto {

    @JsonProperty("nickName")
    private String username;

    @JsonProperty("userId")
    private String uid;

    private String avatar;  //qq头像

    @JsonProperty("admin")
    private Boolean isAdmin;

}
