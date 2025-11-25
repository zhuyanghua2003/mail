package com.msb.mall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.msb.common.valid.groups.AddGroupsInterface;
import com.msb.common.valid.groups.UpdateGroupsInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.msb.mall.product.entity.BrandEntity;
import com.msb.mall.product.service.BrandService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;

import javax.validation.Valid;


/**
 * Ʒ?
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 16:56:12
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
   /* @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Valid @RequestBody BrandEntity brand, BindingResult result){
        if (result.hasErrors()){
            Map<String,String> map=new HashMap<>();
            List<FieldError> fieldErrors = result.getFieldErrors();
            for (FieldError fieldError : fieldErrors){
                String field = fieldError.getField();
                String defaultMessage = fieldError.getDefaultMessage();
                map.put(field,defaultMessage);
            }
            return R.error(400,"提交的品牌表单数据不合法").put("data",map);
        }
		brandService.save(brand);

        return R.ok();
    }*/

    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated(AddGroupsInterface.class) @RequestBody BrandEntity brand){

        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroupsInterface.class) @RequestBody BrandEntity brand){
        /*boolean b = brandService.updateById(brand);*/
        brandService.updateDeatil(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

    @GetMapping("/all")
    public R gerAllBrand(){
        BrandEntity entity=new BrandEntity();
        entity.setName("华为");
        return R.ok().put("brands",entity);
    }



}
