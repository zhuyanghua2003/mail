package com.msb.mall.product.service.impl;

import com.msb.common.constant.ProductConstant;
import com.msb.mall.product.dao.AttrAttrgroupRelationDao;
import com.msb.mall.product.dao.AttrGroupDao;
import com.msb.mall.product.entity.AttrAttrgroupRelationEntity;
import com.msb.mall.product.entity.AttrGroupEntity;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.AttrAttrgroupRelationService;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.AttrGroupRelationVO;
import com.msb.mall.product.vo.AttrResponseVO;
import com.msb.mall.product.vo.AttrVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.AttrDao;
import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    private AttrGroupServiceImpl attrGroupService;
    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVO vo) {
        AttrEntity attrEntity=new AttrEntity();
        BeanUtils.copyProperties(vo,attrEntity);
        this.save(attrEntity);
        if (vo.getAttrGroupId()!=null && vo.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity=new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(vo.getAttrGroupId());
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBasePage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_type","base".equalsIgnoreCase(attrType)?1:0);
        if (catelogId!=0){
            wrapper.eq("catelog_id",catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty( key)){
            wrapper.and((obj) ->{
                obj.eq("attr_id", key)
                        .or()
                        .like("attr_name", key);
            });
        }



        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVO> list = records.stream().map(item -> {
            AttrResponseVO attrResponseVO = new AttrResponseVO();
            BeanUtils.copyProperties(item, attrResponseVO);
            CategoryEntity categoryEntity = categoryService.getById(item.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVO.setCatelogName(categoryEntity.getName());
            }
            if ("base".equalsIgnoreCase(attrType)){
                AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
                entity.setAttrId(item.getAttrId());
                //attrAttrgroupRelationService.query(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",entity.getAttrId()))
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao
                        .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", item.getAttrId()));
                if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                    //获取到属性组的id，根据属性组的ID获取属性组的名称
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrResponseVO.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }


            return attrResponseVO;
        }).collect(Collectors.toList());
        pageUtils.setList(list);
        return pageUtils;
    }

    @Override
    public AttrResponseVO getAttrInfo(Long attrId) {
        AttrResponseVO responseVO=new AttrResponseVO();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,responseVO);
        if (attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity!=null){
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());

                if (attrGroupEntity!=null){
                    responseVO.setAttrGroupId(attrGroupEntity.getAttrGroupId());
                    responseVO.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        Long catelogId = attrEntity.getCatelogId();
        Long[] catelgPath = categoryService.findCatelgPath(catelogId);
        responseVO.setCatelogPath(catelgPath);
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        if (categoryEntity!=null){
            responseVO.setCatelogName(categoryEntity.getName());
        }


        return responseVO;
    }

    @Transactional
    @Override
    public void updateBaseAttr(AttrVO attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr,entity);
        this.updateById(entity);
        if (entity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(entity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", entity.getAttrId()));
            if (count>0){
                attrAttrgroupRelationDao.update(relationEntity,new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", entity.getAttrId()));
            }else {
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }


    }

    @Override
    public void removeByIdsDetails(Long[] attrIds) {

        for (Long attrId : attrIds){
            AttrEntity attrEntity = getById(attrId);
            if (attrEntity!=null && attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
                attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrId));
            }
        }
        this.removeByIds(Arrays.asList(attrIds));
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<AttrEntity> attrEntities = list.stream().map((entity) -> {
            AttrEntity attrEntity = this.getById(entity.getAttrId());
            return attrEntity;
        }).filter((entity)->entity != null).collect(Collectors.toList());
        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVO[] vos) {
        List<AttrAttrgroupRelationEntity> list = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, entity);
            return entity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.removeBatchRelation( list);
    }

    @Autowired
    AttrGroupDao attrGroupDao;
    @Override
    public PageUtils getNoAttrRelation(Map<String, Object> params, Long attrgroupId) {
        // 1.查询当前属性组所在的类别编号
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrgroupId);
        // 获取到对应的分类id
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2.当前分组只能关联自己所属的类别下其他的分组没有关联的属性信息。
        // 先找到这个类别下的所有的分组信息
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 获取属性组的编号集合
        List<Long> groupIds = group.stream().map((g) -> g.getAttrGroupId()).collect(Collectors.toList());
        // 然后查询出类别信息下所有的属性组已经分配的属性信息
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        List<Long> attrIds = relationEntities.stream().map((m) -> m.getAttrId()).filter(attrId -> attrId != null).collect(Collectors.toList());
        // 根据类别编号查询所有的属性信息并排除掉上面的属性信息即可
        // 这其实就是需要查询出最终返回给调用者的信息了  分页  带条件查询
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id",catelogId)
                // 查询的是基本属性信息，不需要查询销售属性信息
                .eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        // 然后添加排除的条件
        if(attrIds != null && attrIds.size() > 0){
            wrapper.notIn("attr_id",attrIds);
        }
        // 还有根据key的查询操作
        String key = (String)params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        // 新增：打印最终SQL
        System.out.println("最终SQL: " + wrapper.getSqlSegment());
        System.out.println("参数: " + wrapper.getParamNameValuePairs());

        // 查询对应的相关信息
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}