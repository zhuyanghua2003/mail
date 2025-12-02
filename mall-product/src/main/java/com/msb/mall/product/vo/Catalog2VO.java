package com.msb.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catalog2VO {

    private  String catalog1Id;
    private List<Catalog3VO> catalog3List;
    private String id;
    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catalog3VO{
        private String catalog2Id;
        private String id;
        private String name;
    }

}
