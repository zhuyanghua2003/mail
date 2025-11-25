package com.msb.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.coupon.entity.CouponSpuCategoryRelationEntity;

import java.util.Map;

/**
 * ?Ż?ȯ????????
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 18:45:28
 */
public interface CouponSpuCategoryRelationService extends IService<CouponSpuCategoryRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

