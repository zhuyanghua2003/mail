package com.msb.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.dto.SeckillOrderDto;
import com.msb.common.exception.NoStockExecption;
import com.msb.common.utils.PageUtils;
import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.vo.OrderConfirmVo;
import com.msb.mall.order.vo.OrderResponseVO;
import com.msb.mall.order.vo.OrderSubmitVO;
import com.msb.mall.order.vo.PayVo;

import java.util.Map;

/**
 * ????
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 18:05:14
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    OrderResponseVO submitOrder(OrderSubmitVO vo) throws NoStockExecption;

    PayVo getOrderPay(String orderSn);

    void updateOrderStatus(String orderSn,Integer status);

    void handleOrderComplete(String orderSn);

    void quickCreateOrder(SeckillOrderDto orderDto);

}

