package com.wangpan.service;

import com.wangpan.dto.ShareSessionDto;
import com.wangpan.dto.WebShareInfoDto;
import com.wangpan.entity.po.FileInfo;
import com.wangpan.entity.vo.FileVO;
import com.wangpan.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * @author fangyixin
 * @date 2023/12/7 20:41
 */

public interface WebShareService {

    /**
     * 返回指定分享文件的信息
     * @param shareId
     * @return
     */
    WebShareInfoDto getShareInfoCommon(String shareId);

    /**
     * 分享码验证
     */
    ShareSessionDto checkCode(String shareId, String code);

    PaginationResultVO<FileInfo> getList(String shareId, String filePid);

    /**
     * @param path：上一级目录的fileId
     * @param shareUserId：文件拥有者的Id
     * @return 存储上一级的文件数据
     */
    List<FileVO> getFolderInfo(String path, String shareUserId);

    /**
     * 文件转存
     * @param uid：当前登录用户
     * @param shareUserId：文件分享者
     * @param shareFileIds：转存文件ID
     * @param myFolderId：，目标转存目录地址
     */
    void save2MyAccount(String uid, String shareUserId, String shareFileIds, String myFolderId);
}
