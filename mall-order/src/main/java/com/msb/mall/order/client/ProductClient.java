package com.msb.mall.order.client;

import com.msb.common.utils.R;
import com.msb.mall.order.vo.OrderItemSpuInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("mall-product")
public interface ProductClient {

    @GetMapping("/product/brand/all")
    public R gerAllBrand();

    @GetMapping("/product/spuinfo/getOrderItemSpuInfoBySpuId/{spuIds}")
    public List<OrderItemSpuInfoVO> getOrderItemSpuInfoBySpuId(@PathVariable("spuIds") Long[] spuIds);
}
