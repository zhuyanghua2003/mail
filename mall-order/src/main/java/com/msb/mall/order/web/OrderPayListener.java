package com.msb.mall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@Controller
public class OrderPayListener {


    @RequestMapping("/payed/notify")
    public void handleAlipayed(HttpServletRequest request){
        System.out.println("----------------->支付成功的回调接口");
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keys = parameterMap.keySet();
        for (String key : keys) {
            System.out.println(key + ":" + Arrays.toString(parameterMap.get(key)));
        }
        System.out.println("支付宝回调支付成功");
    }


    @ResponseBody
    @RequestMapping("test")
    public String test(){
        return "success";
    }
}
