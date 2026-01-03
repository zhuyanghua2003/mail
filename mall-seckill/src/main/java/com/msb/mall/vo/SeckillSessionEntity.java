package com.msb.mall.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 秒杀活动场次
 * 
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-12-18 22:57:00
 */
@Data
public class SeckillSessionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */

	private Long id;
	/**
	 * 场次名称
	 */
	private String name;
	/**
	 * 每日开始时间
	 */
	@JsonFormat(
			pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // 匹配前端的"2025-12-17T16:00:00.000Z"格式
			timezone = "UTC" // 对应字符串中的"Z"（代表UTC时区）
	)
	private Date startTime;
	/**
	 * 每日结束时间
	 */
	@JsonFormat(
			pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // 匹配前端的"2025-12-17T16:00:00.000Z"格式
			timezone = "UTC" // 对应字符串中的"Z"（代表UTC时区）
	)
	private Date endTime;
	/**
	 * 启用状态
	 */
	private Integer status;
	/**
	 * 创建时间
	 */
	@JsonFormat(
			pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // 匹配前端的"2025-12-17T16:00:00.000Z"格式
			timezone = "UTC" // 对应字符串中的"Z"（代表UTC时区）
	)
	private Date createTime;

	private List<SeckillSkuRelationEntity> relationEntities;

}
