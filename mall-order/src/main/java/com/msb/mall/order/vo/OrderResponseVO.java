package com.msb.mall.order.vo;

import com.msb.mall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderResponseVO {

    private OrderEntity orderEntity;
    private Integer code;

}
