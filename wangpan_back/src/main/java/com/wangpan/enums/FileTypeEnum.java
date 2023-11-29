package com.wangpan.enums;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 文件类型枚举
 * @author fangyixin
 * @date 2023/11/29 13:02
 */

public enum FileTypeEnum {
    VIDEO(1,FileCategoryEnum.VIDEO,new String[]{".mp4",".avi",".rmvb",".mkv",".mov"},"视频"),
    MUSIC(2,FileCategoryEnum.MUSIC,new String[]{".mp3",".wav",".wma",".mp2",".flac",".midi",".ra",".ape",".aac",".cda"},"音乐"),
    IMAGE(3,FileCategoryEnum.IMAGE,new String[]{".jpeg",".jpg",".png",".gif",".bmp",".dds",".psd",".pdt",".webp",".xmp"},"图片"),
    PDF(4,FileCategoryEnum.DOC,new String[]{".pdf"},"pdf"),
    WORD(5,FileCategoryEnum.DOC,new String[]{".docx",".doc"},"word"),
    EXCEL(6,FileCategoryEnum.DOC,new String[]{".xlsx"},"excel"),
    TXT(7,FileCategoryEnum.DOC,new String[]{".txt"},"txt文档"),
    PROGRAME(8,FileCategoryEnum.OTHERS,new String[]{".h",".c",".hpp",".hxx",".cpp",".cc",".c++",".cxx",".m",".o",
            ".s",".dll",".cs",".java",".class",".js",".ts",".css",".scss",".vue",".jsx",".sql",".md",".json",".html",".xml"},"code代码文件"),
    ZIP(9,FileCategoryEnum.OTHERS,new String[]{".rar",".zip",".7z",".cab",".arj",".lzh",".tar",".gz",".ace",".uue",
            ".bz",".jar",".iso",".mpq"},"zip压缩文件"),
    OTHERS(10,FileCategoryEnum.OTHERS,new String[]{},"其他");

    private Integer type;
    private FileCategoryEnum category;
    private String[] suffix;
    private String desc;

    FileTypeEnum(Integer type, FileCategoryEnum category, String[] suffix, String desc) {
        this.type = type;
        this.category = category;
        this.suffix = suffix;
        this.desc = desc;
    }

    public static FileTypeEnum getFileTypeBySuffix(String suffix){
        for(FileTypeEnum item: FileTypeEnum.values()){
            if(ArrayUtils.contains(item.getSuffix(), suffix)){
                return item;
            }
        }
        return FileTypeEnum.OTHERS;
    }

    public static FileTypeEnum getByType(Integer type){
        for(FileTypeEnum item: FileTypeEnum.values()){
            if(item.getType().equals(type)){
                return item;
            }
        }
        return FileTypeEnum.OTHERS;
    }

    public Integer getType() {
        return type;
    }

    public FileCategoryEnum getCategory() {
        return category;
    }

    public String[] getSuffix() {
        return suffix;
    }

    public String getDesc() {
        return desc;
    }
}
