package com.msb.mall.coupon.service.impl;

import com.msb.common.dto.MemberPrice;
import com.msb.common.dto.SkuReductionDTO;
import com.msb.mall.coupon.entity.MemberPriceEntity;
import com.msb.mall.coupon.entity.SkuLadderEntity;
import com.msb.mall.coupon.service.SkuLadderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.coupon.dao.SkuFullReductionDao;
import com.msb.mall.coupon.entity.SkuFullReductionEntity;
import com.msb.mall.coupon.service.SkuFullReductionService;


@Slf4j
@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    private SkuLadderService ladderService;
    @Autowired
    private MemberPriceServiceImpl memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionDTO dto) {
        log.info("保存商品满减信息,收到的信息为{}",dto);
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(dto.getSkuId());
        ladderEntity.setFullCount(dto.getFullCount());
        ladderEntity.setDiscount(dto.getDiscount());
        ladderEntity.setAddOther(dto.getCountStatus());
        if (ladderEntity.getFullCount()>0){
            ladderService.save(ladderEntity);
        }
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(dto,fullReductionEntity);
        if (fullReductionEntity.getFullPrice().compareTo(new BigDecimal(0))==1){
            boolean save = this.save(fullReductionEntity);
            log.info("保存商品满减信息,保存结果为{}",save);
        }
        if (dto.getMemberPrice()!=null && dto.getMemberPrice().size()>0){
            log.info("保存商品会员价格信息,会员价格为{}",dto.getMemberPrice());
            List<MemberPriceEntity> memberPriceEntities = dto.getMemberPrice().stream().map(item -> {
                MemberPriceEntity priceEntity = new MemberPriceEntity();
                priceEntity.setSkuId(dto.getSkuId());
                priceEntity.setMemberLevelId(item.getId());
                priceEntity.setMemberLevelName(item.getName());
                priceEntity.setMemberPrice(item.getPrice());
                priceEntity.setAddOther(1);
                return priceEntity;
            }).collect(Collectors.toList());
            boolean b = memberPriceService.saveBatch(memberPriceEntities);
            log.info("保存商品会员价格信息,保存结果为{}",b);
        }




    }

}