package com.msb.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.service.AttrAttrgroupRelationService;
import com.msb.mall.product.service.AttrService;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.service.impl.AttrServiceImpl;
import com.msb.mall.product.vo.AttrGroupRelationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.msb.mall.product.entity.AttrGroupEntity;
import com.msb.mall.product.service.AttrGroupService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;



/**
 * ???Է??
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 16:56:12
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @PostMapping("/attr/relation")
    public R saveBatch(@RequestBody List<AttrGroupRelationVO> vos){
        attrAttrgroupRelationService.saveBatch(vos);
        return R.ok();
    }

    @PostMapping("/attr/relation/delete")
    public R relationDelete(@RequestBody AttrGroupRelationVO[] vos){
        attrService.deleteRelation(vos);
        return R.ok();
    }

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params){
        PageUtils pageUtils=attrService.getNoAttrRelation(params,attrgroupId);
        System.out.println(attrgroupId);
        System.out.println(pageUtils);
        return R.ok().put("page", pageUtils);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrGroupService.queryPage(params);

        return R.ok().put("page", page);
    }
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> list= attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", list);
    }

    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId){
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        System.out.println(catelogId);
        Long[] paths =categoryService.findCatelgPath(catelogId);
        attrGroup.setCatelogPath(paths);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }
    @GetMapping("/{catelogId}/withattr")
    public R getAttrgroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupEntity> list=attrGroupService.getAttrgroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", list);
    }
}
