package com.msb.mall.product.web;

import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.Catalog2VO;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Trace
    @GetMapping({"/","/home","/index"})
    public String index(Model  model) {
        List<CategoryEntity> list= categoryService.getLevelCategory();
        model.addAttribute("categories",list);
        return "index";
    }
    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<Catalog2VO>> getCategory(){
        Map<String, List<Catalog2VO>> map =categoryService.getCatelog2JSON();
        /*Map<String, List<Catalog2VO>> map =null;*/
        return map;
    }
    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

}
