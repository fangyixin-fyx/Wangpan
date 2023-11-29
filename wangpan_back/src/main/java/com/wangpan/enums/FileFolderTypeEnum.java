package com.wangpan.enums;

/**
 * @author fangyixin
 * @date 2023/11/29 13:54
 */
public enum FileFolderTypeEnum {
    FILE(0,"文件"),
    FOLDER(1,"目录");

    private Integer type;
    private String description;

    FileFolderTypeEnum(Integer type, String description) {
        this.type = type;
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
