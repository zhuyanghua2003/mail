package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.common.constant.ProductConstant;
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
import org.springframework.util.CollectionUtils;
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

    @Transactional
    @Override
    public void up(Long spuId) {
        // 1.根据spuId查询相关的信息 封装到SkuESModel对象中
        List<SkuESModel> skuEs = new ArrayList<>();
        // 根据spuID找到对应的SKU信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        log.info("【商品上架】根据spuId={}查询到SKU列表，数量={}，SKU详情={}",
                spuId, CollectionUtils.isEmpty(skus) ? 0 : skus.size(), skus);
        if (CollectionUtils.isEmpty(skus)) {
            log.warn("【商品上架】spuId={}未查询到任何SKU信息，上架流程终止", spuId);
            return;
        }

        // 对应的规格参数  根据spuId来查询规格参数信息
        List<SkuESModel.Attrs> attrsModel = getAttrsModel(spuId);
        log.info("【商品上架】根据spuId={}查询到SKU列表，数量={}，SKU详情={}",
                spuId, CollectionUtils.isEmpty(skus) ? 0 : skus.size(), skus);
        if (CollectionUtils.isEmpty(attrsModel)) {
            log.warn("【商品上架】spuId={}未查询到可检索规格属性（attrs），后续ES中attrs字段为空", spuId);
        }
        // 需要根据所有的skuId获取对应的库存信息---》远程调用
        List<Long> skuIds = skus.stream().map(sku -> {
            return sku.getSkuId();
        }).collect(Collectors.toList());
        log.info("【商品上架】待查询库存的SKU ID列表={}", skuIds);
        Map<Long, Boolean> skusHasStockMap = getStatus(skuIds);
        log.info("【商品上架】库存查询结果：skuHasStockMap={}", skusHasStockMap);
        if (skusHasStockMap == null || skusHasStockMap.isEmpty()) {
            log.warn("【商品上架】库存查询结果为空，默认所有SKU标记为有库存");
        }
        // 2.远程调用mall-search的服务，将SukESModel中的数据存储到ES中
        List<SkuESModel> skuESModels = skus.stream().map(item -> {
            SkuESModel model = new SkuESModel();
            // 先实现属性的复制
            BeanUtils.copyProperties(item,model);
            model.setSubTitle(item.getSkuTitle());
            model.setSkuPrice(item.getPrice());
            model.setSkuImg(item.getSkuDefaultImg());

            // hasStock 是否有库存 --》 库存系统查询  一次远程调用获取所有的skuId对应的库存信息
            if(skusHasStockMap == null){
                model.setHasStock(true);
            }else{
                model.setHasStock(skusHasStockMap.get(item.getSkuId()));
            }
            // hotScore 热度分 --> 默认给0即可
            model.setHotScore(0l);
            // 品牌和类型的名称
            BrandEntity brand = brandService.getById(item.getBrandId());
            CategoryEntity category = categoryService.getById(item.getCatalogId());
            model.setBrandName(brand.getName());
            model.setBrandImg(brand.getLogo());
            model.setCatalogName(category.getName());
            // 需要存储的规格数据
            model.setAttrs(attrsModel);

            return model;
        }).collect(Collectors.toList());
        // 将SkuESModel中的数据存储到ES中
        R r = searchFeginService.productStatusUp(skuESModels);
        // 3.更新SPUID对应的状态
        // 根据对应的状态更新商品的状态
        log.info("----->ES操作完成：{}" ,r.getCode());
        System.out.println("-------------->"+r.getCode());
        if(r.getCode() == 0){
            // 远程调用成功  更新商品的状态为 上架
            baseMapper.updateSpuStatusUp(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else{
            // 远程调用失败
        }
    }

    @Override
    public List<OrderItemSpuInfoVO> getOrderItemSpuInfoBySpuId(Long[] spuIds) {
        List<OrderItemSpuInfoVO> list = new ArrayList<>();
        for (Long spuId : spuIds){
            OrderItemSpuInfoVO vo=new OrderItemSpuInfoVO();
            SpuInfoEntity spuInfoEntity=this.getById(spuId);
            vo.setId(spuInfoEntity.getId());
            vo.setSpuName(spuInfoEntity.getSpuName());
            vo.setBrandId(spuInfoEntity.getBrandId());
            vo.setCatalogId(spuInfoEntity.getCatalogId());

            BrandEntity brand = brandService.getById(spuInfoEntity.getBrandId());
            vo.setBrandName(brand.getName());
            CategoryEntity category = categoryService.getById(spuInfoEntity.getCatalogId());
            vo.setCatalogName(category.getName());

            SpuInfoDescEntity descEntity = spuInfoDescService.getById(spuId);
            vo.setImg(descEntity.getDecript());
            list.add(vo);
        }
        return list;
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
        // 1. product_attr_value 存储了对应的spu相关的所有的规格参数
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrsForSpuId(spuId);
        log.info("spuId={}，查询到的基础属性baseAttrs={}", spuId, baseAttrs);
        if (CollectionUtils.isEmpty(baseAttrs)) {
            log.warn("spuId={}未查询到任何基础属性！", spuId);
            return new ArrayList<>();
        }
        // 2. attr  search_type 决定该属性是否支持检索
        List<Long> attrIds = baseAttrs.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        // 查询出所有的可以检索的对应的规格参数编号
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
        // baseAttrs中根据可以检索的数据过滤
        List<SkuESModel.Attrs> attrsModel = baseAttrs.stream().filter(item -> {
            return searchAttrIds.contains(item.getAttrId());
        }).map(item -> {
            SkuESModel.Attrs attr = new SkuESModel.Attrs();
            attr.setAttrId(item.getAttrId());
            attr.setAttrName(item.getAttrName());
            attr.setAttrValue(item.getAttrValue());
            BeanUtils.copyProperties(item, attr);
            log.info("【商品上架】将属性属性值映射为ES模型，属性编号={}，属性名称={}，属性值={}",
                    attr.getAttrId(), attr.getAttrName(), attr.getAttrValue());
            return attr;
        }).collect(Collectors.toList());
        return attrsModel;
    }

}