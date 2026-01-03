package com.msb.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.msb.mall.product.service.CategoryBrandRelationService;
import com.msb.mall.product.vo.Catalog2VO;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.CategoryDao;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public List<CategoryEntity> queryPageWithTree(Map<String, Object> params) {
        //查询所有的商品分类信息
        List<CategoryEntity> categoryEntities=baseMapper.selectList(null);
        //将所有商品分类信息拆解成树形结构[父子关系]
        //第一步遍历出所有的大类 parent_cid=0
        List<CategoryEntity> list = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(categoryEntity -> {
                    //根据大类找到所有的小类，递归的方式实现
                    categoryEntity.setChildren(getCategoryChildren(categoryEntity, categoryEntities));
                    return categoryEntity;

                }).sorted((entity1, entity2) -> {
                    return (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort());
                }).collect(Collectors.toList());

        //第二步根据大类找到对应的所有小类

        return list;
    }

    @Override
    public void removeCategoryByIds(List<Long> ids) {
        //1检查类别数据是否在其他类别中被使用（目前跳过）
        //2.批量逻辑删除
        baseMapper.deleteBatchIds(ids);
    }

    private List<CategoryEntity> getCategoryChildren(CategoryEntity categoryEntity,
                                                     List<CategoryEntity> categoryEntities) {
        return categoryEntities.stream().filter(entity -> entity.getParentCid().equals(categoryEntity.getCatId()))
                .map(entity -> {
                    entity.setChildren(getCategoryChildren(entity, categoryEntities));
                    return entity;
                }).sorted((entity1, entity2) -> {
                    return (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort());
                }).collect(Collectors.toList());
    }

    @Override
    public Long[] findCatelgPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);


        return paths.toArray(new Long[parentPath.size()]);
    }

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Transactional
    //@CacheEvict(value = "category",key = "'getLevelCategory'")
    //@Caching(evict = {@CacheEvict(value = "category",key = "'getLevelCategory'"),
          //  @CacheEvict(value = "category",key = "'getCatelog2JSON'")})
    @CacheEvict(value = "category",allEntries = true)
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())){
            categoryBrandRelationService.updateCatelogName(category.getCatId(), category.getName());
        }
    }

    @Trace
    @Tags({
            @Tag(key = "getLevelCategory", value = "returnObj")
    })
    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevelCategory() {
        log.info("查询了数据库操作");
        List<CategoryEntity> list = baseMapper.queryLeve1Category();
        return list;
    }

    private List<CategoryEntity> queryByParenCid(List<CategoryEntity> list,Long parentCid){
        List<CategoryEntity> collect = list.stream().filter(item -> {
            return item.getParentCid().equals(parentCid);
        }).collect(Collectors.toList());
        return collect;
    }

    private Map<String,Map<String, List<Catalog2VO>>> cache=new HashMap<>();

    //查询所有分类数据并且完成所有一级二级三级分类的关联
    @Trace
    @Tags({
            @Tag(key = "getCatelog2JSON", value = "returnObj")
    })
    @Cacheable(value = {"category"},key = "#root.method.name")
    @Override
    public Map<String, List<Catalog2VO>> getCatelog2JSON(){
        List<CategoryEntity> list= baseMapper.selectList(new QueryWrapper<CategoryEntity>());
        List<CategoryEntity> levelCategory = this.queryByParenCid(list, 0L);
        Map<String, List<Catalog2VO>> map = levelCategory.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            List<CategoryEntity> l2Catalog = this.queryByParenCid(list, value.getCatId());
            List<Catalog2VO> catalog2VOs = null;
            if (l2Catalog != null && l2Catalog.size() > 0){
                catalog2VOs = l2Catalog.stream().map(l2 -> {
                    Catalog2VO catalog2VO = new Catalog2VO(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> lsCatalogs = this.queryByParenCid(list, l2.getCatId());
                    if (lsCatalogs != null && lsCatalogs.size() > 0){
                        List<Catalog2VO.Catalog3VO> catalog3VOStream = lsCatalogs.stream().map(l3 -> {
                            Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3VO;
                        }).collect(Collectors.toList());
                        catalog2VO.setCatalog3List(catalog3VOStream);
                    }
                    return catalog2VO;
                }).collect(Collectors.toList());

            }


            return catalog2VOs;
        }));
        return map;
    }

    public Map<String, List<Catalog2VO>> getCatelog2JSONRedis(){
        String key="getCatelog2JSON";
        String catelog2JSON = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(catelog2JSON)){
            log.info("缓存没有命中。。。。。");
            //缓存中没有数据，从数据库中获取
            Map<String, List<Catalog2VO>> catelog2JSONForDb = getCatelog2JSONForDbWithRedisson();
            return catelog2JSONForDb;
        }
        log.info("缓存命中。。。。。");
        Map<String, List<Catalog2VO>> map = JSON.parseObject(catelog2JSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
        });
        return map;
    }

    //从数据库查询的结果
    public Map<String, List<Catalog2VO>> getCatelog2JSONForDbWithRedisson() {
        String key2="getCatelog2JSON";
        Map<String, List<Catalog2VO>> data = null;
        RLock lock = redissonClient.getLock(key2 + "-lock");
        //加锁
        try {
            lock.lock();
            log.info("获取锁成功。。。。。");
            data = getDataForDB(key2);
        }finally {
            lock.unlock();

        }

        return data;

    }

    //从数据库查询操作
    private Map<String, List<Catalog2VO>> getDataForDB(String key2) {
        log.info("查询数据库。。。。。");
        String catalogJSON=stringRedisTemplate.opsForValue().get(key2);
        if (!StringUtils.isEmpty(catalogJSON)){
            //缓存中有数据，直接返回
            Map<String, List<Catalog2VO>> map = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
            return map;
        }
        List<CategoryEntity> list= baseMapper.selectList(new QueryWrapper<CategoryEntity>());

        List<CategoryEntity> levelCategory = this.queryByParenCid(list, 0L);
        Map<String, List<Catalog2VO>> map = levelCategory.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            List<CategoryEntity> l2Catalog = this.queryByParenCid(list, value.getCatId());
            List<Catalog2VO> catalog2VOs = null;
            if (l2Catalog != null && l2Catalog.size() > 0){
                catalog2VOs = l2Catalog.stream().map(l2 -> {
                    Catalog2VO catalog2VO = new Catalog2VO(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> lsCatalogs = this.queryByParenCid(list, l2.getCatId());
                    if (lsCatalogs != null && lsCatalogs.size() > 0){
                        List<Catalog2VO.Catalog3VO> catalog3VOStream = lsCatalogs.stream().map(l3 -> {
                            Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3VO;
                        }).collect(Collectors.toList());
                        catalog2VO.setCatalog3List(catalog3VOStream);
                    }
                    return catalog2VO;
                }).collect(Collectors.toList());

            }


            return catalog2VOs;
        }));
        //缓存数据
        /* cache.put("getCatelog2JSON",map);*/
        if(map == null){
            // 那就说明数据库中也不存在  防止缓存穿透
            stringRedisTemplate.opsForValue().set(key2,"1",5, TimeUnit.SECONDS);
        }else{
            // 从数据库中查询到的数据，我们需要给缓存中也存储一份
            // 防止缓存雪崩
            String json = JSON.toJSONString(map);
            stringRedisTemplate.opsForValue().set(key2,json,100,TimeUnit.MINUTES);
        }

        return map;
    }

    //从数据库查询的结果
    public Map<String, List<Catalog2VO>> getCatelog2JSONForDb() {
        String key2="getCatelog2JSON";
        synchronized (this){
            return getDataForDB(key2);
        }



    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths){
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

}