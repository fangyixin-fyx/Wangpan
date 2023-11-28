package com.wangpan.enums;

/**
 * @author fangyixin
 * @date 2023/11/27 14:22
 */
public enum FileStatusEnum {
    TRANSFER(0,"转码中"),
    FAIL(1,"转码失败"),
    SUCCESS(2,"转码成功");


    private Integer status;
    private String description;

    FileStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
}
