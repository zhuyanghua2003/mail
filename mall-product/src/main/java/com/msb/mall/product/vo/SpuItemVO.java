package com.msb.mall.product.vo;

import com.msb.mall.product.entity.SkuImagesEntity;
import com.msb.mall.product.entity.SkuInfoEntity;
import com.msb.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SpuItemVO {

    SkuInfoEntity info;

    boolean hasStock=true;

    List<SkuImagesEntity> images;

    List<SkuItemSaleAttrVO> saleAttrs;

    SpuInfoDescEntity desc;

    List<SpuItemGroupAttrVO> baseAttrs;

    SeckillVO seckillVO;



}
