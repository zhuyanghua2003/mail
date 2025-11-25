package com.msb.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.member.entity.MemberEntity;

import java.util.Map;

/**
 * ??Ա
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 18:48:53
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

