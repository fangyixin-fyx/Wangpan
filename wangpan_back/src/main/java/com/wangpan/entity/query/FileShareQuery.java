package com.wangpan.entity.query;

import lombok.Data;

import java.util.Date;

@Data
public class FileShareQuery extends BaseQuery {
	private String shareId;
	private String shareIdFuzzy;
	private String fileId;
	private String fileIdFuzzy;
	private String userId;
	private String userIdFuzzy;
	// 有效期  0：1天  1：7天   2：30天   3：永久
	private Integer validType;
	// 失效时间
	private Date expireTime;
	private String expireTimeStart;
	private String expireTimeEnd;
	// 分享时间
	private Date shareTime;
	private String shareTimeStart;
	private String shareTimeEnd;
	// 提取码
	private String code;
	private String codeFuzzy;
	// 被浏览次数
	private Integer showCount;
	// 0:自主定义  1:系统生成
	private Integer codeType;

	private Boolean queryFileName;

}