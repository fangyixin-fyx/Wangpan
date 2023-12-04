package com.wangpan.dto;

import lombok.Data;

/**
 * @author fangyixin
 * @date 2023/12/4 10:49
 */
@Data
public class DownloadFileDto {
    private String downloadCode;
    private String fileName;
    private String filePath;
}
