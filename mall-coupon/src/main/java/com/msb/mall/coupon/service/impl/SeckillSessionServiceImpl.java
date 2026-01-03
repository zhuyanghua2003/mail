package com.msb.mall.coupon.service.impl;

import com.msb.common.utils.R;
import com.msb.mall.coupon.entity.SeckillSkuRelationEntity;
import com.msb.mall.coupon.service.SeckillSkuRelationService;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.coupon.dao.SeckillSessionDao;
import com.msb.mall.coupon.entity.SeckillSessionEntity;
import com.msb.mall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    SeckillSkuRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Trace
    @Tags({
            @Tag(key = "getLatest3Days", value = "returnObj")
    })
    @Override
    public List<SeckillSessionEntity> getLatest3Days() {
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(),endTime()));
        List<SeckillSessionEntity> newList = list.stream().map(session -> {
            List<SeckillSkuRelationEntity> relationList = relationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
            session.setRelationEntities(relationList);
            return session;
        }).collect(Collectors.toList());
        return newList;
    }

    private String startTime(){
        LocalDateTime start = LocalDateTime.of(LocalDate.now().plusDays(0), LocalTime.MIN);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    };
    private String endTime(){
        LocalDateTime end= LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    };


    public static void main(String[] args) {
        LocalDate now = LocalDate.now();




        LocalDateTime end= LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX);
        System.out.println(end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));


    }

}