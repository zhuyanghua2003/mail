package com.msb.mall.search.controller;

import com.msb.common.dto.es.SkuESModel;
import com.msb.common.exception.BizCodeEnume;
import com.msb.common.utils.R;
import com.msb.mall.search.service.ElasticSearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSearchSaveController {
    @Autowired
    private ElasticSearchSaveService service;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuESModel> skuESModels) {
        Boolean b = false;
        try {
             b= service.productStatusUp(skuESModels);
        } catch (IOException e) {
            log.error("ElasticSearchSaveController.productStatusUp error:{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (!b) {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }

        return R.ok();
    }


}
