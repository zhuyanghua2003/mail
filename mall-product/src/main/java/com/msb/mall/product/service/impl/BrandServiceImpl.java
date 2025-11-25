package com.msb.mall.product.service.impl;

import com.msb.mall.product.dao.CategoryBrandRelationDao;
import com.msb.mall.product.entity.CategoryBrandRelationEntity;
import com.msb.mall.product.service.CategoryBrandRelationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.BrandDao;
import com.msb.mall.product.entity.BrandEntity;
import com.msb.mall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    CategoryBrandRelationDao relationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key =(String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)){
            wrapper.eq("brandId",key).or().like("name",key);
        }


        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Transactional
    @Override
    public void updateDeatil(BrandEntity brand) {
        this.updateById(brand);
        if (!StringUtils.isEmpty(brand.getName())){
            categoryBrandRelationService.updateBrandName(brand.getBrandId(),brand.getName());
        }
    }



}