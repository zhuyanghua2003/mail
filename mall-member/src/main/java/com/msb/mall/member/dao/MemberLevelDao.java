package com.msb.mall.member.dao;

import com.msb.mall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ??Ա?ȼ?
 * 
 * @author dpb
 * @email dengpbs@163.com
 * @date 2025-11-18 18:48:53
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity queryMemberLevelDefault();

}
