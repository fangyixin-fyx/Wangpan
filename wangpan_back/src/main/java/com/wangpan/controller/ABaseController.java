package com.wangpan.controller;

import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.enums.ResponseCodeEnum;

public class ABaseController {

    protected static final String STATUE_SUCCESS="success";
    protected static final String STATUE_ERROR="error";

    protected <T> ResponseVO getSuccessResponseVO(T t){
        ResponseVO<T> responseVO=new ResponseVO<>();
        responseVO.setStatus(STATUE_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setDate(t);
        return responseVO;
    }
}
