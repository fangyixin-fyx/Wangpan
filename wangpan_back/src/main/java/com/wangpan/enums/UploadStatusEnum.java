package com.wangpan.enums;

/**
 * @author fangyixin
 * @date 2023/11/28 19:29
 */
public enum UploadStatusEnum {
    UPLOAD_SECOND("upload_seconds","秒传"),
    UPLOADING("uploading","上传中"),
    UPLOADED("upload_finish","上传成功");

    private String code;
    private String description;

    UploadStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
