package com.msb.mall.product.service.impl;

import com.msb.mall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

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
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())){
            categoryBrandRelationService.updateCatelogName(category.getCatId(), category.getName());
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