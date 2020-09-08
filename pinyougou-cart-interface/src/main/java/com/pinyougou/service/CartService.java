package com.pinyougou.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * 购物车服务接口
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-06-27 下午 04:00
 */
public interface CartService {
    /**
     * 添加商品到购物车
     * @param list
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> list, Long itemId, Integer num);
}
