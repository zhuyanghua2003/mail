package com.msb.mall.ware.service.impl;

import com.msb.common.dto.SkuHasStockDto;
import com.msb.common.exception.NoStockExecption;
import com.msb.common.utils.R;
import com.msb.mall.ware.feign.ProductFeignService;
import com.msb.mall.ware.vo.LockStockResult;
import com.msb.mall.ware.vo.OrderItemVo;
import com.msb.mall.ware.vo.WareSkuLockVO;
import lombok.Data;
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
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVO vo) {
        List<OrderItemVo> items = vo.getItems();
        List<SkuWareHasStock> collect = items.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setSkuId(item.getSkuId());
            List<WareSkuEntity> wareSkuEntities=this.baseMapper.listHashStock(item.getSkuId());
            skuWareHasStock.setWareSkuEntities(wareSkuEntities);
            skuWareHasStock.setNum(item.getCount());
            return skuWareHasStock;
        }).collect(Collectors.toList());
        for (SkuWareHasStock skuWareHasStock : collect){
            Long skuId = skuWareHasStock.getSkuId();
            List<WareSkuEntity> wareSkuEntities = skuWareHasStock.getWareSkuEntities();
            if (wareSkuEntities == null || wareSkuEntities.size() == 0){
                throw new NoStockExecption(skuId);
            }
            Integer count= skuWareHasStock.getNum();
            System.out.println(count);
            Boolean skuStock = false;
            for (WareSkuEntity wareSkuEntity : wareSkuEntities){
                Integer canStock= wareSkuEntity.getStock()-wareSkuEntity.getStockLocked();
                System.out.println(canStock);
                if (count<= canStock){
                    Integer i= this.baseMapper.lockSkuStock(skuId,wareSkuEntity.getWareId(),count);
                    System.out.println(i);
                    count=0;
                    skuStock=true;
                }else {
                    Integer i= this.baseMapper.lockSkuStock(skuId,wareSkuEntity.getWareId(),canStock);
                    System.out.println(i);
                    count=count-canStock;
                }
                if (count<=0){
                    break;
                }
            }
            if (count>0){
                log.error("库存没有锁定完");
                throw new NoStockExecption(skuId);
            }
            if (skuStock == false){
                log.error("库存不足 || 库存锁定没有成功");
                throw  new NoStockExecption(skuId);
            }
        }


        return true;
    }

    @Transactional
    @Override
    public Boolean orderDeductStock(WareSkuLockVO vo) {
        List<OrderItemVo> items = vo.getItems();
        if (items == null || items.isEmpty()) {
            log.warn("订单商品项为空，无需扣减库存");
            return true;
        }

        // 遍历每个商品项，扣减库存并释放锁定
        for (OrderItemVo item : items) {
            Long skuId = item.getSkuId();
            Integer deductNum = item.getCount(); // 需要扣减的数量（和之前锁定的数量一致）

            if (skuId == null || deductNum == null || deductNum <= 0) {
                log.warn("skuId[{}]扣减数量不合法：{}", skuId, deductNum);
                continue;
            }

            // 1. 查询该sku已锁定库存的仓库（优先扣减已锁定的仓库）
            List<WareSkuEntity> lockedWareSkus = this.baseMapper.listLockedStock(skuId);
            if (lockedWareSkus == null || lockedWareSkus.isEmpty()) {
                log.error("skuId[{}]无锁定库存记录，无法扣减", skuId);
                throw new NoStockExecption(skuId); // 抛自定义异常回滚事务
            }

            Integer remainDeductNum = deductNum; // 剩余需要扣减的数量
            // 2. 逐个仓库扣减（和上锁逻辑一致，优先扣减有库存的仓库）
            for (WareSkuEntity wareSku : lockedWareSkus) {
                Long wareId = wareSku.getWareId();
                Integer lockedStock = wareSku.getStockLocked(); // 该仓库已锁定的数量
                Integer actualStock = wareSku.getStock(); // 该仓库实际库存

                // 校验：锁定库存不足，无法扣减
                if (lockedStock < remainDeductNum) {
                    log.error("skuId[{}]仓库[{}]锁定库存不足：锁定{}，需扣减{}",
                            skuId, wareId, lockedStock, remainDeductNum);
                    throw new NoStockExecption(skuId);
                }

                // 计算本次扣减数量：不超过剩余需扣减数量，且不超过锁定数量
                Integer currentDeductNum = Math.min(remainDeductNum, lockedStock);
                // 校验：实际库存不足（防止超卖）
                if (actualStock < currentDeductNum) {
                    log.error("skuId[{}]仓库[{}]实际库存不足：实际{}，需扣减{}",
                            skuId, wareId, actualStock, currentDeductNum);
                    throw new NoStockExecption(skuId);
                }

                // 3. 原子性扣减：实际库存 - N，锁定库存 - N（核心SQL操作）
                int updateCount = this.baseMapper.deductStockAndReleaseLock(
                        skuId, wareId, currentDeductNum
                );

                if (updateCount <= 0) {
                    log.error("skuId[{}]仓库[{}]扣减库存失败", skuId, wareId);
                    throw new RuntimeException("库存扣减失败：skuId=" + skuId + ", wareId=" + wareId);
                }

                log.info("skuId[{}]仓库[{}]扣减库存成功：扣减{}，剩余需扣减{}",
                        skuId, wareId, currentDeductNum, remainDeductNum - currentDeductNum);

                // 剩余扣减数量清零，跳出循环
                remainDeductNum -= currentDeductNum;
                if (remainDeductNum <= 0) {
                    break;
                }
            }

            // 4. 校验：所有仓库扣减后，仍有未扣减的数量
            if (remainDeductNum > 0) {
                log.error("skuId[{}]扣减库存未完成，剩余需扣减{}", skuId, remainDeductNum);
                throw new NoStockExecption(skuId);
            }
        }

        log.info("订单库存扣减完成，订单号：{}", vo.getOrderSN()); // 假设VO中有订单号字段
        return true;
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<WareSkuEntity> wareSkuEntities;
    }
}