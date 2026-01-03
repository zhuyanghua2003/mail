package com.msb.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.msb.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class AlipayTemplate {

    // 商户appid 沙箱账号: ykxslx3672@sandbox.com
    public static String APPID = "9021000156616629";
    // 私钥 pkcs8格式的
    public static String RSA_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC50puVNw4MauOshfSY+IkxRBelSKsdxTsuxZ6+co7HaeLMQo1m6DRAzvLaJPskI9MOzYqROVCudcwjfaoqa7wA/qkZvCSkKu/WvN4ZLbvESoI6ybCwGocenR/M5OEVHYBDWsm5S+rZLDotCvVL25QEWigNN2St/eNgVIetOBlZdccOMI7sH2YUR0xv233DzvU3rtyWRstOBGoEWjQ8dEg5O+LW/BSjgHQfz9RaX4JygJqn/6wRRihgIgwxPYVBRuKLWTbNwBCVrNeQgT6ooiCTr5iEsFKJCp5LIhH9Ul7PFctNroYG3Pp5eM3E9gTkZPfGHnAxQ/immf33Y4DCa0RjAgMBAAECggEBAKyU2ODNwCNyRr/drZ1A/xIdfqSIiDxfCKdY7SVN+iNkyToR+YgA0z75tX066XDYQNY3uaFFDRTq3Mx9ksRRSMvXuK/pGFLMefIDeXcDDaex15/H4Gy+Ro39OiKu1AkrXAuyTzXkN1q6A9H/onBwn9km7pnPv5cE/N9jwprzRj/ZV0RveJexLMu656djRkPAL523VtnAA+huG1eXE7z3l8RKTyGPI40nMsUVS2xEbHmPyWYFgzqm4+ih4YrpJcEbdxNuGLZ09sPCO6A8+nZp9YWWJmmFADvfifxDIxukLquPfMZlHwyfaTVFlsodee1Wj29jdRVeL6CUafKYS/9Me3kCgYEA70t8WbL9ylUgU/Gp3WzQSKNjmD0qXsnQHEFoeDI5bvxa5EFqvV8ZGRlF1cm7H8cWyKF9t/RaP4fmClAn6uWIYEGAL7VmOn51igKvtqHhFRrxPpPYEuo/5DvlFBFIhKCTytowJzou0jWj2RyMrouKqdwO1NO0Zp1E1g7TcWb4kK8CgYEAxsuA9pR2k/Uw1x0fRwk4xuiXxzbKoRAeEF+jIAJ1z+WiwQCMM2km0yyPXg2e74iKjQMCOl4UJRvS712jkwFiReEzQ7dtcXdFKwv/NFLC3K3G6zJlYNSdm0ktl7HOg6WDSqOtQGNHWnfDSmrPlRQn52zjzxgu+lxBfbrI54ISrI0CgYAR5vzTnR99jqbPgOnrZeOjO44M6q6Lzt9h7cbKDz/avMh0ASziJAY1qX1fBCuasgNTWTmVlJNX42vjY6HI4gk04lGbwB94ytMyUwcgS7YSJSTdQKROrfW/tndp6+0X9wd3iqFFngW4tkieypL/z/K8yHjXlUsOzj9DIHLTLhf0gwKBgETjyuooYrs9UdKJ9gfQ60sK2WYg17Fe+3wjGE2EbL1Bz9fgT7WVL+4oV/p47+YERDc/nqsJZSPC86MdqQzNgvfCZdiGnTef5GlZe/Pu5jZBRljFGxqmM4PXpSQXawB1//yCogxZGyX/sL5+Kzd3R6jMNFnVAJqkE9H1Q5/ulHgVAoGAdYktX7nLMkQ6xLqLYsNXkATNBJvBSLEYT0EatD77QGC0IMEabYjsU+e/f4JlUQiS1uT/7n+1MkvRIUmdt/zUssPtC8rwkX79hsMDqAYh7gXhTc36gFlKxpRFY2bOJjK+d4+tPuskFmQL4klLhXkANMkcEFaEMrTEixZgjAyhZc8=";
    // 服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://frpclient04.xhyonline.com:17240/payed/notify";
    // 页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
    public static String return_url = "http://order.msb.com/orderPay/returnUrl";
    // 请求网关地址
    public static String URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    // 编码
    public static String CHARSET = "UTF-8";
    // 返回格式
    public static String FORMAT = "json";
    // 支付宝公钥
    public static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjmfRyzq0ieVHInlUhVgGyIIKUyK/BEwzLglKkoM+eEd9yJTEMjyDUMXNKnvMLsYB033vAEDyBeCZau/vSj/65yxKDwY4FZe7OnBLlDc3CTsVHh/2gl2RTBX9DSSJqYTdCzWTwuaGpbqiJB9fTi8TbRFe/+OPY1gxxi6t9KmYfjSKLWm6Z90PcUftVnAu3xxQHXUsIf0uEhmsTMCRMrW+jbL17imhWHK46yeIaTZmoENdnovWUNzWXItw2fdwx4goVk/XOdxpzTOgzWxLNAJ8A2PphuHx6gQjF1CvezR1k54MZd2v6Y6PiQREggOVcr/MEJyU2ANy0I2qHHP7MbbDcQIDAQAB";
    // 日志记录目录
    public static String log_path = "/log";
    // RSA2
    public static String SIGNTYPE = "RSA2";

    public String pay(PayVo payVo){
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        //调用RSA签名方式
        AlipayClient client = new DefaultAlipayClient(URL,
                APPID,
                RSA_PRIVATE_KEY,
                FORMAT,
                CHARSET,
                ALIPAY_PUBLIC_KEY,
                SIGNTYPE);
        AlipayTradeWapPayRequest alipay_request=new AlipayTradeWapPayRequest();

        // 封装请求支付信息
        AlipayTradeWapPayModel model=new AlipayTradeWapPayModel();
        model.setOutTradeNo(payVo.getOut_trader_no());
        model.setSubject(payVo.getSubject());
        model.setTotalAmount(payVo.getTotal_amount());
        model.setBody(payVo.getBody());
        model.setTimeoutExpress("15m");
        model.setProductCode("QUICK_WAP_WAY");
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(notify_url);
        // 设置同步地址
        alipay_request.setReturnUrl(return_url);

        // form表单生产
        String form = "";
        try {
            // 调用SDK生成表单
            form = client.pageExecute(alipay_request).getBody();
            return form;
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return  null;
    }
}
