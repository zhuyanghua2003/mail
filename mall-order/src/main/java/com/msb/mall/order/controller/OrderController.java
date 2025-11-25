package com.msb.mall.order.controller;

import java.util.Arrays;
import java.util.Map;

import com.msb.mall.order.client.ProductClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.service.OrderService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;



/**
 * ????
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 18:05:14
 */
@RefreshScope
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductClient productClient;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }
    @GetMapping("/products")
    public R queryProducts(){
        return R.ok().put("products", productClient.gerAllBrand());
    }
    @Value("${user.names}")
    private String name;


    @Value("${user.age}")
    private String age;

    @GetMapping("/users")
    public R queryUser(){
        System.out.println("name:"+name+" age:"+age);
        return R.ok().put("name", name).put("age", age);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
