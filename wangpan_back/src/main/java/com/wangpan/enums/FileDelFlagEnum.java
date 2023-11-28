package com.wangpan.enums;

/**
 * @author fangyixin
 * @date 2023/11/27 14:22
 */
public enum FileDelFlagEnum {
    DEL(0,"删除"),
    RECYCLE(1,"回收站"),
    USING(2,"正常");


    private Integer status;
    private String description;

    FileDelFlagEnum(Integer status, String description) {
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
