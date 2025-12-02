package com.msb.mall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.msb.mall.ware.vo.MergeVO;
import com.msb.mall.ware.vo.PurchaseDoneVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.msb.mall.ware.entity.PurchaseEntity;
import com.msb.mall.ware.service.PurchaseService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;



/**
 * ?ɹ???Ϣ
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 18:41:49
 */
@RestController
@RequestMapping("ware/purchase")
@Slf4j
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }

    @PostMapping("/receive")
    public R receive(@RequestBody List<Long> ids){
        purchaseService.receive(ids);

        return R.ok();

    }

    @RequestMapping("/merge")
    //@RequiresPermissions("ware:purchase:list")
    public R merge(@RequestBody MergeVO mergeVO){
        Integer flag = purchaseService.merge(mergeVO);
        if (flag == -1){
            return R.error("合并失败....该采购单不能被合并");
        }


        return R.ok();
    }
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVO vo){
        purchaseService.done(vo);
        return R.ok();
    }

    @RequestMapping("/unreceive/list")
    //@RequiresPermissions("ware:purchase:list")
    public R listUnreceive(@RequestParam Map<String, Object> params){
        //PageUtils page = purchaseService.queryPage(params);
        PageUtils page = purchaseService.queryPageUnreceive(params);
        System.out.println("page:"+page);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
