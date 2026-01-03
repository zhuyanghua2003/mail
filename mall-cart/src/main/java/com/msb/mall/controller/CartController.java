package com.msb.mall.controller;


import com.msb.common.constant.AuthConstant;
import com.msb.common.vo.MemberVO;
import com.msb.mall.Interceptor.AuthInterceptor;
import com.msb.mall.service.ICartService;
import com.msb.mall.vo.Cart;
import com.msb.mall.vo.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Controller
public class CartController {
    @Autowired
    private ICartService cartService;

    @ResponseBody
    @GetMapping("/getUserCartItems")
    public List<CartItem> getUserCartItems(){
        List<CartItem> userCartItems = cartService.getUserCartItems();
        for (CartItem cartItem : userCartItems){
            log.info("购物车商品：{}",cartItem);
        }
        return userCartItems;
    }

    //清空购物车中的已支付的商品
    @ResponseBody
    @PostMapping("/deleteCart")
    public boolean deleteCart(){
        boolean flag= cartService.deleteCart();
        return flag;
    }

    @GetMapping("/cart_list")
    public String queryCartList(Model  model){
        Cart cart = cartService.getCartList();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @GetMapping("/addCart")
    public String addCart(@RequestParam("skuId") Long skuId
            , @RequestParam("num") Integer num,
                          Model  model)  {
        CartItem item= null;
        try {
            item = cartService.addCart(skuId,num);
        } catch (Exception e) {
            log.error("添加购物车失败");
            e.printStackTrace();
        }
        model.addAttribute("item",item);
        return "success";
    }




}
