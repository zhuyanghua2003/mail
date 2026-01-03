package com.msb.mall.order.fegin;

import com.msb.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient("mall-cart")
public interface CartFeginService {

    @GetMapping("/getUserCartItems")
    List<OrderItemVo> getUserCartItems();

    @PostMapping("/deleteCart")
    public boolean deleteCart();
}
