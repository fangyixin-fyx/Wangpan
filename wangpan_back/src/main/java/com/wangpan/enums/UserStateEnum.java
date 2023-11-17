package com.wangpan.enums;

/**
 * 用户状态枚举
 * @author fangyixin
 * @date 2023/11/17 17:05
 */
public enum UserStateEnum {
    DISABLE(0,"禁用"),
    ABLE(1,"启用");

    private Integer state;
    private String desc;

    UserStateEnum(Integer state,String desc){
        this.state=state;
        this.desc=desc;
    }

    public Integer getState() {
        return state;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static UserStateEnum getByState(Integer state){
        for(UserStateEnum item: UserStateEnum.values()){
            if(item.getState().equals(state)) return item;
        }
        return null;
    }
}
