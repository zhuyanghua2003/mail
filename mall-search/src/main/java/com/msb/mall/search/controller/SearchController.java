package com.msb.mall.search.controller;

import com.msb.mall.search.service.MallSearchService;
import com.msb.mall.search.vo.SearchParam;
import com.msb.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@Controller
public class SearchController {
    @Autowired
    private MallSearchService mallSearchService;

    //http://search.msb.com/list.html?catalog3Id=225
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model  model){
        SearchResult result = mallSearchService.search(param);
       // System.out.println(result.toString());
        model.addAttribute("result",result);


        return "index";
    }


}
