package com.msb.mall.ware.service.impl;

import com.msb.common.constant.WareConstant;
import com.msb.mall.ware.entity.PurchaseDetailEntity;
import com.msb.mall.ware.service.PurchaseDetailService;
import com.msb.mall.ware.service.WareSkuService;
import com.msb.mall.ware.vo.MergeVO;
import com.msb.mall.ware.vo.PurchaseDoneVO;
import com.msb.mall.ware.vo.PurchaseItemDoneVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.ware.dao.PurchaseDao;
import com.msb.mall.ware.entity.PurchaseEntity;
import com.msb.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
@Slf4j
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService detailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        // 添加查询的条件
        queryWrapper.eq("status",0).or().eq("status",1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public Integer merge(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();
        if (purchaseId == null){
            // 没有指定采购单ID，则新建一个采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        PurchaseEntity purchaseEntity = this.getById(purchaseId);
        if(purchaseEntity.getStatus() > WareConstant.PurchaseStatusEnum.RECEIVE.getCode()){
            // 该菜单不能合单
            return -1;
        }
        List<Long> items = mergeVO.getItems();
        final long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> list = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGED.getCode());
            return detailEntity;
        }).filter(item -> {
            return item.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() || item.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGED.getCode();
        }).collect(Collectors.toList());
        if (list != null && list.size() > 0){
            detailService.updateBatchById(list);
        }



        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(purchaseId);
        entity.setUpdateTime(new Date());
        this.updateById(entity);

        return 1;
    }

    @Transactional
    @Override
    public void receive(List<Long> ids) {
        List<PurchaseEntity> list = ids.stream().map(id -> {
            return this.getById(id);
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setUpdateTime(new Date());
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            return item;
        }).collect(Collectors.toList());
        this.updateBatchById(list);
        for (Long id : ids) {
            List<PurchaseDetailEntity> detailEntities = detailService.listDetailByPurchaseId(id);
            List<PurchaseDetailEntity> collect = detailEntities.stream().map(item -> {
                PurchaseDetailEntity entity = new PurchaseDetailEntity();
                entity.setId(item.getId());
                entity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return entity;
            }).collect(Collectors.toList());
            detailService.updateBatchById(collect);
        }



    }

    @Transactional
    @Override
    public void done(PurchaseDoneVO vo) {
        //获取采购单的编号
        Long id = vo.getId();
        //1.改变采购项的状态
        Boolean flag=true;
        List<PurchaseItemDoneVO> items = vo.getItems();
        List<PurchaseDetailEntity> list=new ArrayList<>();
        for (PurchaseItemDoneVO item : items){
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                //该采购项出现了问题
                log.error("有采购项出现异常,{}", item);
                flag=false;
                detailEntity.setStatus(item.getStatus());
            }else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                PurchaseDetailEntity detailEntity1 = detailService.getById(item.getItemId());
                wareSkuService.addStock(detailEntity1.getSkuId(), detailEntity1.getWareId(), detailEntity1.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            list.add(detailEntity);

        }
        detailService.updateBatchById( list);
        //2.改变采购单的状态
        PurchaseEntity purchaseEntity=new PurchaseEntity();
        purchaseEntity.setId( id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode():WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
        //3.将采购成功的采购项进行入库操作

    }

}