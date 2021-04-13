package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

/**
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-06-27 下午 07:38
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    private static final Logger logger = LogManager.getLogger();
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //1.从cookie中提取购物车
            List<Cart> cartList = findCartList();
            //2.调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")) {
                logger.info("开始将购物车存入Cookie中");
                //3.将新的购物车存入cookie
                String cartListString = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", cartListString, 3600 * 24, "UTF-8");
            } else {
                logger.info("开始将购物车存入Redis中");
                cartService.saveCartListToRRedis(username, cartList);
            }
            return new Result(true, "存入购物车成功！");
        } catch (Exception e) {
            logger.error("存入购物车失败，CartController.addGoodsToCartList() called with itemId = {}, num = {}",
                itemId, num, e);
            return new Result(false, "存入购物车失败！");
        }
    }

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //当前登录账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("开始从Cookie中查找购物车列表");
        //从cookie中提取购物车
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_Cookie = JSON.parseArray(cartListString, Cart.class);

        if (username.equals("anonymousUser")) {//未登录
            logger.info("从Cookie提取购物车执行结束，结果：[{}]", JSON.toJSONString(cartList_Cookie));
            return cartList_Cookie;
        } else {//已登录
            //合并购物车逻辑
            logger.info("开始从Redis中查找购物车列表");
            List<Cart> cartList_Redis = cartService.findCartListFromRedis(username);
            if (cartList_Cookie.size() < 0) {
                // RECORD [MrLu] [2020-09-13 12:11] : 在本地购物车为空的情况下不执行合并逻辑，直接返回redis中存储的购物车列表
                return cartList_Redis;
            }
            List<Cart> cartList = cartService.mergeCartList(cartList_Cookie, cartList_Redis);
            //将合并后的的购物车存入Redis中
            cartService.saveCartListToRRedis(username, cartList);
            //清除本地Cookie中存储的购物车
            CookieUtil.deleteCookie(request, response, "cartList");

            logger.info("从Redis查找购物车列表并合并Cookie购物车执行结束，Redis: [{}], Cookie: [{}], 结果：[{}]",
                JSON.toJSONString(cartList_Redis), JSON.toJSONString(cartList_Cookie), JSON.toJSONString(cartList));
            return cartList;
        }
    }
}