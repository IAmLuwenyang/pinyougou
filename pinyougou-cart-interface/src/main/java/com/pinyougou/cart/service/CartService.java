package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;
import java.util.List;

/**
 * 购物车服务接口
 *
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-06-27 下午 04:00
 */
public interface CartService {

    /**
     * 添加商品到购物车
     *
     * @param list
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> list, Long itemId, Integer num);

    /**
     * 从 Redis中提取购物车
     *
     * @param username
     * @return
     */
    List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车列表存储到 Redis中
     *
     * @param username
     * @param cartList
     */
    void saveCartListToRRedis(String username, List<Cart> cartList);

    /**
     * 合并购物车
     *
     * @param cartList1
     * @param cartList2
     * @return
     */
    List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
}
