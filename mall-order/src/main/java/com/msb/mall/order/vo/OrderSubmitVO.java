package com.msb.mall.order.vo;

import lombok.Data;

@Data
public class OrderSubmitVO {

    private Long addrId;

    private Integer payType;

    private String orderToken;

    private String note;
}
