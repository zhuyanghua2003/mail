package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * ??Ʒ???????
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 16:56:12
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> queryPageWithTree(Map<String, Object> params);

    void removeCategoryByIds(List<Long> list);

    Long[] findCatelgPath(Long catelogId);


    void updateDetail(CategoryEntity category);
}

