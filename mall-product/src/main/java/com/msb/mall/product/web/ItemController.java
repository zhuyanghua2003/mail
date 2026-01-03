package com.msb.mall.product.web;

import com.msb.mall.product.service.SkuInfoService;
import com.msb.mall.product.vo.SpuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {
    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping(value = "/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model  model) throws ExecutionException, InterruptedException {
        SpuItemVO spuItemVO = skuInfoService.item(skuId);
        model.addAttribute("item",spuItemVO);
        System.out.println(skuId);
        System.out.println(spuItemVO);


        return "item";
    }
}
