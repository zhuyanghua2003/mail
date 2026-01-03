package com.msb.mall.controller;

import com.alibaba.fastjson.JSON;
import com.msb.common.utils.R;
import com.msb.mall.dto.SeckillSkuRedisDto;
import com.msb.mall.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SeckillController {
    @Autowired
    SeckillService seckillService;

    @GetMapping("/getCurrentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisDto> currentSeckillSkus = seckillService.getCurrentSeckillSkus();

        return R.ok().put("data", JSON.toJSONString(currentSeckillSkus));
    }

    @GetMapping("/getSeckillSessionBySkuId")
    @ResponseBody
    public R getSeckillSessionBySkuId(@RequestParam("skuId") Long skuId){
        log.info("开始查询{}的秒杀信息",skuId);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        SeckillSkuRedisDto seckillSkuRedisDto = seckillService.getSeckillSessionBySkuId(skuId);
        return R.ok().put("data", JSON.toJSONString(seckillSkuRedisDto));

    }
    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId, @RequestParam("code") String code, @RequestParam("num") Integer num, Model model){
        String orderSN = seckillService.kill(killId,code,num);
        System.out.println("orderSN:"+orderSN);
        model.addAttribute("orderSn",orderSN);
        return "success";
    }
}
