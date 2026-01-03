package com.msb.mall.order.web;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.msb.common.constant.OrderConstant;
import com.msb.common.exception.NoStockExecption;
import com.msb.mall.order.config.AlipayTemplate;
import com.msb.mall.order.service.OrderService;
import com.msb.mall.order.vo.OrderConfirmVo;
import com.msb.mall.order.vo.OrderResponseVO;
import com.msb.mall.order.vo.OrderSubmitVO;
import com.msb.mall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private AlipayTemplate alipayTemplate;

    @GetMapping("/orderPay/returnUrl")
    public String orderPay(@RequestParam(value = "orderSn",required = false) String orderSn,
                           @RequestParam(value = "out_trade_no",required = false) String out_trade_no){
        // TODO 完成相关的支付操作
        System.out.println("orderSn = " + orderSn);
        System.out.println("out_trade_no = " + out_trade_no);
        if (StringUtils.isNotBlank(orderSn)){
            orderService.handleOrderComplete(orderSn);
           // orderService.updateOrderStatus(orderSn, OrderConstant.OrderStateEnum.TO_SEND_GOODS.getCode());
        }else {
            orderService.handleOrderComplete(out_trade_no);
           // orderService.updateOrderStatus(out_trade_no, OrderConstant.OrderStateEnum.TO_SEND_GOODS.getCode());
        }
        
        return "list";
    }

    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn){
        PayVo payVo= orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        System.out.println("pay:"+pay);

        return "hello:"+pay;
    }

    @GetMapping("/toTrade")
    public String toTrade(Model model) {

        OrderConfirmVo confirmVo= orderService.confirmOrder();
        model.addAttribute("confirmVo", confirmVo);

        return "confirm";
    }
    @PostMapping("/orderSubmit")
    public String orderSubmit(OrderSubmitVO vo, Model model, RedirectAttributes redirectAttributes) throws NoStockExecption {
        Integer code=0;
        OrderResponseVO responseVO=null;
        try {
            responseVO = orderService.submitOrder(vo);
            code = responseVO.getCode();

        }catch (NoStockExecption e){
            code=2;
        }
        if (code==0){
            //下单操作成功
            model.addAttribute("orderResponseVO",responseVO);
            return "pay";
        }else {
            //下单失败
            System.out.println("code "+code);
            String msg = "下单失败";
            if (code==1){
                msg=msg+";重复提交";
            }
            if (code==2){
                msg=msg+";锁定库存失败";
            }
            redirectAttributes.addFlashAttribute("msg",msg);

            return "redirect:http://order.msb.com/toTrade";
        }



    }
}
