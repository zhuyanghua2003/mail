package com.msb.mall.ware.dao;

import com.msb.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    List<WareSkuEntity> listHashStock(@Param("skuId") Long skuId);

    Integer lockSkuStock(Long skuId, Long wareId, Integer count);

    List<WareSkuEntity> listLockedStock(@Param("skuId") Long skuId);

    int deductStockAndReleaseLock(@Param("skuId") Long skuId,@Param("wareId") Long wareId, @Param("currentDeductNum") Integer currentDeductNum);

}
