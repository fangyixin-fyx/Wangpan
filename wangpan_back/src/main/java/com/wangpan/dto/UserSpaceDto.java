package com.wangpan.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fangyixin
 * @date 2023/11/17 20:03
 */
@Data
public class UserSpaceDto implements Serializable {
    private Long userSpace;
    private Long totalSpace;
}
