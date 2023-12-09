package com.wangpan.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author fangyixin
 * @date 2023/12/8 15:05
 */
@Data
public class ShareSessionDto {
    private String shareId;
    private String shareUserId;
    private Date expireTime;
    private String fileId;
}
