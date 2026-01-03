package com.msb.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class SpuItemGroupAttrVO {
    private String groupName;
    private List<Attr> baseAttrs;
}
