package com.msb.mall.ware.service.impl;

import com.msb.common.dto.SkuHasStockDto;
import com.msb.common.utils.R;
import com.msb.mall.ware.feign.ProductFeignService;
import lombok.extern.slf4j.Slf4j;
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

import com.msb.mall.ware.dao.WareSkuDao;
import com.msb.mall.ware.entity.WareSkuEntity;
import com.msb.mall.ware.service.WareSkuService;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuDao skuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        List<WareSkuEntity> list = skuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (list == null || list.size() == 0){
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(skuId);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            entity.setStockLocked(0);
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data=(Map<String,Object>) info.get("skuInfo");
                if (info.getCode()==0){
                    entity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){
                log.error("远程服务调用失败");
            }


            skuDao.insert(entity);
        }else {
            skuDao.addStock(skuId,wareId,skuNum);

        }

    }

    @Override
    public List<SkuHasStockDto> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockDto> list = skuIds.stream().map(skuId -> {
            Long count = baseMapper.getSkuStock(skuId);
            count = count == null ? 0 : count;
            SkuHasStockDto dto = new SkuHasStockDto();
            dto.setSkuId(skuId);
            dto.setHasStock(count > 0);
            return dto;
        }).collect(Collectors.toList());
        return list;
    }

}