package com.msb.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.common.utils.R;
import com.msb.mall.product.dao.SkuImagesDao;
import com.msb.mall.product.dao.SkuInfoDao;
import com.msb.mall.product.entity.SkuImagesEntity;
import com.msb.mall.product.entity.SkuInfoEntity;
import com.msb.mall.product.entity.SpuInfoDescEntity;
import com.msb.mall.product.fegin.SeckillFeignService;
import com.msb.mall.product.service.AttrGroupService;
import com.msb.mall.product.service.SkuInfoService;
import com.msb.mall.product.service.SkuSaleAttrValueService;
import com.msb.mall.product.service.SpuInfoDescService;

import com.msb.mall.product.vo.SeckillVO;
import com.msb.mall.product.vo.SkuItemSaleAttrVO;
import com.msb.mall.product.vo.SpuItemGroupAttrVO;
import com.msb.mall.product.vo.SpuItemVO;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuInfoDao skuInfoDao;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;





    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private SkuImagesDao skuImagesDao;
    @Autowired
    private SkuImagesServiceImpl skuImagesService;
    @Autowired
    SeckillFeignService seckillFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * SKU 信息检索的方法
     * 类别
     * 品牌
     * 价格区间
     * 检索的关键字
     * 分页查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        // 检索关键字
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
               w.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        // 分类
        String catalogId = (String)params.get("catalogId");
        if(!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }
        // 品牌
        String brandId = (String)params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        // 价格区间
        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            wrapper.ge("price",min);
        }
        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(max)){
            try {
                // 如果max=0那么我们也不需要加这个条件
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal(0)) == 1){
                    // 说明 max > 0
                    wrapper.le("price",max);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据spu查询所有的sku信息
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return list;
    }

    @Trace
    @Tags({
            @Tag(key="item",value = "returnedObj")
            ,@Tag(key="itemParam",value = "arg[0]")
    })


    @Override
    public List<String> getSkuSaleAttrs(Long skuId) {

        return this.skuInfoDao.getSkuSaleAttrs(skuId);
    }

    @Trace
    @Tags({
            @Tag(key = "item", value = "returnObj"),
            @Tag(key = "itemParam", value = "arg[0]")
    })
    @Override
    public SpuItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        SpuItemVO spuItemVO = new SpuItemVO();
        CompletableFuture<SkuInfoEntity> skuInfofuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = getById(skuId);
            spuItemVO.setInfo(skuInfoEntity);

            return skuInfoEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> sakeFuture = skuInfofuture.thenAcceptAsync((res) -> {
            List<SkuItemSaleAttrVO> saleAttrs = skuSaleAttrValueService.getSkuSaleAttrValueBySpuId(res.getSpuId());
            spuItemVO.setSaleAttrs(saleAttrs);
        }, threadPoolExecutor);

        CompletableFuture<Void> spuFuture = skuInfofuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            spuItemVO.setDesc(spuInfoDescEntity);
        }, threadPoolExecutor);

        CompletableFuture<Void> groupFuture = skuInfofuture.thenAcceptAsync((res) -> {
            List<SpuItemGroupAttrVO> groupAttrVO = attrGroupService.getArrtgroupWithSpuId(res.getSpuId(), res.getCatalogId());
            spuItemVO.setBaseAttrs(groupAttrVO);
        }, threadPoolExecutor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            spuItemVO.setImages(images);
        }, threadPoolExecutor);

        CompletableFuture<Void>  seckillFuture = CompletableFuture.runAsync(() -> {
            //查询商品的秒杀活动
            R r = seckillFeignService.getSeckillSessionBySkuId(skuId);
            if(r.getCode() == 0){
                //查询成功
                SeckillVO seckillVO= JSON.parseObject(r.get("data").toString(),SeckillVO.class);
                spuItemVO.setSeckillVO(seckillVO);
            }

        }, threadPoolExecutor);



        CompletableFuture.allOf(sakeFuture,spuFuture,groupFuture,imageFuture,seckillFuture).get();
        return spuItemVO;
    }

}