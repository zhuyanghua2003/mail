package com.msb.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * ??Ʒ???
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 18:41:49
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

