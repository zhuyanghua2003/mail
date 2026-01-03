package com.msb.mall.order.dto;

import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateTO {
    private OrderEntity orderEntity;
    private List<OrderItemEntity> orderItemEntitys;
}
