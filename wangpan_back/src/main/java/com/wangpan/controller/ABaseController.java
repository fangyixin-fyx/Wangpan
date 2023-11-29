package com.wangpan.controller;

import com.wangpan.constants.Constants;
import com.wangpan.dto.UserDto;
import com.wangpan.entity.vo.PaginationResultVO;
import com.wangpan.entity.vo.ResponseVO;
import com.wangpan.enums.ResponseCodeEnum;
import com.wangpan.utils.CopyUtil;
import com.wangpan.utils.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ABaseController {

    protected static final String STATUE_SUCCESS="success";
    protected static final String STATUE_ERROR="error";

    private static final Logger logger= LoggerFactory.getLogger(ABaseController.class);

    protected <T> ResponseVO getSuccessResponseVO(T t){
        ResponseVO<T> responseVO=new ResponseVO<>();
        responseVO.setStatus(STATUE_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    //读文件流
    protected void readFile(HttpServletResponse response,String filePath){
        if(!StringTool.pathIsOk(filePath))  return;
        OutputStream out=null;
        FileInputStream in=null;
        try{
            File file=new File(filePath);
            if(!file.exists()) return;
            in=new FileInputStream(file);
            byte[] byteData=new byte[1024];
            out = response.getOutputStream();
            int len=0;
            while((len=in.read(byteData))!=-1){
                //每次读取len长度数据
                out.write(byteData,0,len);
            }
            out.flush();
        }catch (Exception e){
            logger.error("读取文件异常",e);
        }finally {
            if(out!=null){
                try {
                    out.close();
                }catch (IOException e){
                    logger.error("IO异常",e);
                }
            }
            if(in!=null){
                try {
                    in.close();
                }catch (IOException e){
                    logger.error("IO异常",e);
                }
            }
        }
    }

    //从session获取登陆对象信息
    protected UserDto getUserInfoFromSession(HttpSession session){
        UserDto userDto =(UserDto) session.getAttribute(Constants.SESSION_USER);
        return userDto;
    }

    /**
     * 将PO转为VO
     * @param result
     * @param clazz
     * @return
     */
    protected <S,T> PaginationResultVO<T> convert2PaginationVO(PaginationResultVO<S> result, Class<T> clazz){
        PaginationResultVO<T> resultVO=new PaginationResultVO<>();
        resultVO.setList(CopyUtil.copyList(result.getList(),clazz));
        resultVO.setPageNo(result.getPageNo());
        resultVO.setPageSize(result.getPageSize());
        resultVO.setPageTotal(result.getPageTotal());
        resultVO.setTotalCount(result.getTotalCount());
        return resultVO;
    }

}
