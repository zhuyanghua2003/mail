package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.common.dto.MemberPrice;
import com.msb.common.dto.SkuHasStockDto;
import com.msb.common.dto.SkuReductionDTO;
import com.msb.common.dto.SpuBoundsDTO;
import com.msb.common.dto.es.SkuESModel;
import com.msb.common.utils.R;
import com.msb.mall.product.entity.*;
import com.msb.mall.product.fegin.CouponFeginService;
import com.msb.mall.product.fegin.SearchFeginService;
import com.msb.mall.product.fegin.WareFeginService;
import com.msb.mall.product.service.*;
import com.msb.mall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    AttrService attrService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    CouponFeginService couponFeginService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    BrandService brandService;
    @Autowired
    WareFeginService wareFeginService;
    @Autowired
    SearchFeginService searchFeginService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuInfoVO spuInfo) {
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        spuInfoEntity.setPublishStatus(0);
        boolean save1 = this.save(spuInfoEntity);
        if (!save1){
            log.error("保存商品失败,参数s为{}",spuInfoEntity);
        }else {
            log.info("保存商品成功,参数为{}",spuInfoEntity);
        }
        List<String> decripts = spuInfo.getDecript();
        SpuInfoDescEntity descEntity=new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",",decripts));
        boolean save = spuInfoDescService.save(descEntity);
        if (!save){
            log.error("保存商品描述失败，参数为{}",descEntity);
        }else {
            log.info("保存商品描述成功,参数为{}",descEntity);
        }

        List<String> images = spuInfo.getImages();
        List<SpuImagesEntity> imagesEntities = images.stream().map(item -> {
            SpuImagesEntity entity = new SpuImagesEntity();
            entity.setSpuId(spuInfoEntity.getId());
            entity.setImgUrl(item);
            return entity;
        }).collect(Collectors.toList());
        boolean b = spuImagesService.saveBatch(imagesEntities);
        if (!b){
            log.error("保存商品图片失败,参数为{}",imagesEntities);
        }else {
            log.info("保存商品图片成功,参数为{}",imagesEntities);
        }

        List<BaseAttrs> baseAttrs = spuInfo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity entity = new ProductAttrValueEntity();
            entity.setSpuId(spuInfoEntity.getId());
            entity.setAttrId(attr.getAttrId());
            entity.setAttrValue(attr.getAttrValues());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            entity.setAttrName(attrEntity.getAttrName());
            entity.setQuickShow(attr.getShowDesc());
            return entity;
        }).collect(Collectors.toList());
        boolean b1 = productAttrValueService.saveBatch(productAttrValueEntities);
        if (!b1){
            log.error("保存商品属性失败,参数为{}",productAttrValueEntities);
        }else {
            log.info("保存商品属性成功,参数为{}",productAttrValueEntities);
        }

        List<Skus> skus = spuInfo.getSkus();
        if (skus != null && skus.size() > 0){
            skus.forEach(item -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSaleCount(0L);
                List<Images> images1 = item.getImages();
                int i=0;
                for (Images image2 : images1){
                    if (image2.getDefaultImg() == 1){
                        skuInfoEntity.setSkuDefaultImg(image2.getImgUrl());
                        i=1;
                    }
                }
                if (i==0) skuInfoEntity.setSkuDefaultImg("");
                boolean save2 = skuInfoService.save(skuInfoEntity);
                if (!save2){
                    log.error("保存商品sku失败,参数为{}",skuInfoEntity);
                }else {
                    log.info("保存商品sku成功,参数为{}",skuInfoEntity);
                }
                List<SkuImagesEntity> skuImagesEntities = images1.stream().map(img -> {
                    SkuImagesEntity entity = new SkuImagesEntity();
                    entity.setSkuId(skuInfoEntity.getSkuId());
                    entity.setImgUrl(img.getImgUrl());
                    entity.setDefaultImg(img.getDefaultImg());
                    return entity;
                }).filter(img->{
                    return img.getDefaultImg() == 1;
                }).collect(Collectors.toList());
                boolean b2 = skuImagesService.saveBatch(skuImagesEntities);
                if (!b2){
                    log.error("保存商品sku图片失败,参数为{}",skuImagesEntities);
                }else {
                    log.info("保存商品sku图片成功,参数为{}",skuImagesEntities);
                }

                SkuReductionDTO dto=new SkuReductionDTO();
                BeanUtils.copyProperties(item,dto);
                dto.setSkuId(skuInfoEntity.getSkuId());
                // 设置会员价
                if(item.getMemberPrice() != null && item.getMemberPrice().size() > 0){
                    List<MemberPrice> list = item.getMemberPrice().stream().map(memberPrice -> {
                        MemberPrice mDto = new MemberPrice();
                        BeanUtils.copyProperties(memberPrice, mDto);
                        return mDto;
                    }).collect(Collectors.toList());
                    dto.setMemberPrice(list);
                }
                log.info("保存商品优惠信息，传参之前dto为{},item为{}",dto,item);
                R r = couponFeginService.saveFullReductionInfo(dto);
                if (r.getCode() != 0){
                    log.error("调用Coupon服务处理满减、折扣、会员价操作失败,参数为{}",dto);
                }else {
                    log.info("调用Coupon服务处理满减、折扣、会员价操作成功,参数为{}",dto);
                }



                List<Attr> attrs = item.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = attrs.stream().map(sale -> {
                    SkuSaleAttrValueEntity entity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(sale, entity);
                    entity.setSkuId(skuInfoEntity.getSkuId());
                    return entity;
                }).collect(Collectors.toList());
                boolean b3 = skuSaleAttrValueService.saveBatch(saleAttrValueEntities);
                if (!b3){
                    log.error("保存商品sku销售属性失败,参数为{}",saleAttrValueEntities);
                }else {
                    log.info("保存商品sku销售属性成功,参数为{}",saleAttrValueEntities);
                }
            });
            Bounds bounds = spuInfo.getBounds();
            SpuBoundsDTO spuBoundsDTO=new SpuBoundsDTO();
            BeanUtils.copyProperties(bounds,spuBoundsDTO);
            spuBoundsDTO.setSpuId(spuInfoEntity.getId());
            R r = couponFeginService.saveSpuBounds(spuBoundsDTO);
            if (r.getCode() != 0){
                log.error("调用Coupon服务处理积分、成长值操作失败,参数为{}",spuBoundsDTO);
            }else {
                log.info("调用Coupon服务处理积分、成长值操作成功,参数为{}",spuBoundsDTO);
            }


        }

    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key =(String) params.get("key");
        if (!StringUtils.isEmpty( key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key)
                        .or().like("spu_description",key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status", status);
        }

        String catalogId = (String) params.get("catalogId");
        if (!StringUtils.isEmpty(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        List<SpuInfoVO> list = page.getRecords().stream().map(spu -> {
            Long catalogId1 = spu.getCatalogId();
            CategoryEntity categoryEntity = categoryService.getById(catalogId1);
            Long brandId1 = spu.getBrandId();
            BrandEntity brandEntity = brandService.getById(brandId1);
            SpuInfoVO vo = new SpuInfoVO();
            BeanUtils.copyProperties(spu, vo);
            vo.setCatalogName(categoryEntity.getName());
            vo.setBrandName(brandEntity.getName());
            vo.setCatalogId(catalogId1);
            vo.setBrandId(brandId1);
            return vo;
        }).collect(Collectors.toList());
        IPage<SpuInfoVO> iPage=new Page<SpuInfoVO>();
        iPage.setRecords( list);
        iPage.setPages(page.getPages());
        iPage.setCurrent(page.getCurrent());

        return new PageUtils(iPage);
    }

    @Override
    public void up(Long spuId) {
        //根据spuId查询出所有的sku信息，并封装
        List<SkuESModel> skuEs=new ArrayList<>();
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<SkuESModel.Attrs> attrsModel = getAttrsModel(spuId);

        List<Long> skuIds = skus.stream().map(sku -> {
            return sku.getSkuId();
        }).collect(Collectors.toList());
        Map<Long, Boolean> skusHasStockMap = getStatus(skuIds);


        List<SkuESModel> skuESModels = skus.stream().map(item -> {
            SkuESModel model = new SkuESModel();
            BeanUtils.copyProperties(item, model);
            model.setSubTitle(item.getSkuTitle());
            model.setSkuPrice(item.getPrice());

            if (skusHasStockMap == null){
                model.setHasStock(true);
            }else {
                model.setHasStock(skusHasStockMap.get(item.getSkuId()));
            }
            model.setHotScore(0L);
            BrandEntity brand = brandService.getById(item.getBrandId());
            CategoryEntity category = categoryService.getById(item.getCatalogId());
            model.setBrandName(brand.getName());
            model.setBrandImg(brand.getLogo());
            model.setCatalogName(category.getName());
            model.setCatalogId(category.getCatId());
            model.setAttrs(attrsModel);
            return model;
        }).collect(Collectors.toList());


        R r = searchFeginService.productStatusUp(skuESModels);
        if (r.getCode() == 0){
            log.info("调用Search服务处理商品上架成功,参数为{}",skuESModels);
            baseMapper.updateSpuStatusUp(spuId,1);
        }else {
            log.error("调用Search服务处理商品上架失败,参数为{}",skuESModels);
        }


    }

    private Map<Long,Boolean> getStatus(List<Long> spuIds) {
        List<SkuHasStockDto> skusHasStock=null;
        if (spuIds==null || spuIds.size()==0){
            return null;
        }
        try {
            skusHasStock = wareFeginService.getSkusHasStock(spuIds);
            //skusHasStock.stream().collect(Collectors.toMap(item->{return item.getSkuId();},item->{return item.getHasStock();}));
            Map<Long,Boolean> map=skusHasStock.stream()
                    .collect(Collectors.toMap(SkuHasStockDto::getSkuId,item->item.getHasStock()));
            return map;
        }catch (Exception e){
            log.error("调用ware服务查询库存信息失败,错误为{}",e.getMessage());
        }


        return null;
    }

    private List<SkuESModel.Attrs> getAttrsModel(Long spuId) {
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrsForSpuId(spuId);
        List<Long> attrIds = baseAttrs.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        //查询出所有可检索的规格参数编号
        List<Long> searchAttrIds= attrService.selectSearchAttrs(attrIds);

        List<SkuESModel.Attrs> attrsModel = baseAttrs.stream().filter(item -> {
            return searchAttrIds.contains(item.getAttrId());
        }).map(item -> {
            SkuESModel.Attrs attrs = new SkuESModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());
        return attrsModel;
    }

}