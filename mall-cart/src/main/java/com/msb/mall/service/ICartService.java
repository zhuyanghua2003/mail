package com.msb.mall.service;

import com.msb.mall.vo.Cart;
import com.msb.mall.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ICartService {

    public Cart getCartList();

    CartItem addCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    List<CartItem> getUserCartItems();

    boolean deleteCart();

}
