package com.msb.mall.product.vo;

import lombok.Data;

@Data
public class AttrResponseVO extends AttrVO{

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;
}
