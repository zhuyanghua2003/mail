package com.msb.mall.order.vo;

import lombok.Data;

@Data
public class PayVo {

    // 商户订单号
    private String out_trader_no;
    // 订单名称
    private String subject;
    // 付款金额
    private String total_amount;
    // 描述
    private String body;
}
