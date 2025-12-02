package com.msb.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.ware.entity.PurchaseEntity;
import com.msb.mall.ware.vo.MergeVO;
import com.msb.mall.ware.vo.PurchaseDoneVO;

import java.util.List;
import java.util.Map;

/**
 * ?ɹ???Ϣ
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 18:41:49
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    Integer merge(MergeVO mergeVO);

    void receive(List<Long> ids);

    void done(PurchaseDoneVO vo);

}

