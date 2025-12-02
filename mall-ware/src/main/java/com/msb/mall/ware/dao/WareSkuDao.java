package com.msb.mall.ware.dao;

import com.msb.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ??Ʒ???
 * 
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 18:41:49
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {



    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(Long skuId);

}
