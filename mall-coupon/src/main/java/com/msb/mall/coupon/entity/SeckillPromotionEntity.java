package com.msb.mall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 秒杀活动
 * 
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-12-18 22:57:00
 */
@Data
@TableName("sms_seckill_promotion")
public class SeckillPromotionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 活动标题
	 */
	private String title;
	/**
	 * 开始日期
	 */
	@JsonFormat(
			pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // 匹配前端的"2025-12-17T16:00:00.000Z"格式
			timezone = "UTC" // 对应字符串中的"Z"（代表UTC时区）
	)
	private Date startTime;
	/**
	 * 结束日期
	 */
	@JsonFormat(
			pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // 匹配前端的"2025-12-17T16:00:00.000Z"格式
			timezone = "UTC" // 对应字符串中的"Z"（代表UTC时区）
	)
	private Date endTime;
	/**
	 * 上下线状态
	 */
	private Integer status;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 创建人
	 */
	private Long userId;

}
