package com.wangpan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 返还给管理员的用户信息
 * @author fangyixin
 * @date 2023/12/7 13:54
 */
@Data
public class UserForAdminDto {

    @JsonProperty("nickName")
    private String username;

    @JsonProperty("userId")
    private String uid;

    @JsonProperty("qqAvatar")
    private String qqIcon;  //qq头像

    private String email;

    @JsonProperty("joinTime")
    private String registrationTime;

    private String lastLoginTime;

    @JsonProperty("status")
    private Integer state;

    private Integer useSpace;

    private Integer totalSpace;

}
