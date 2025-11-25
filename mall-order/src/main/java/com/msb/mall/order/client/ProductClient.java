package com.msb.mall.order.client;

import com.msb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("mall-product")
public interface ProductClient {

    @GetMapping("/product/brand/all")
    public R gerAllBrand();
}
