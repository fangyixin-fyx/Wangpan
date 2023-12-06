package com.wangpan.enums;

/**
 * 分享有效期枚举类
 * @author fangyixin
 * @date 2023/12/6 19:11
 */
public enum ShareValidTypeEnum {
    DAY_1(0,1,"1天"),
    DAY_7(1,7,"7天"),
    DAY_30(2,30,"30天"),
    FOREVER(3,-1,"永久");

    private Integer type;
    private Integer day;
    private String description;

    ShareValidTypeEnum(Integer type, Integer day, String description) {
        this.type = type;
        this.day = day;
        this.description = description;
    }

    public static ShareValidTypeEnum getByType(Integer type){
        for(ShareValidTypeEnum item:ShareValidTypeEnum.values()){
            if(type== item.getType()){
                return item;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public Integer getDay() {
        return day;
    }

    public String getDescription() {
        return description;
    }
}
