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
        List<Cart> cartList = null;
        //当前登录账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username.equals("anonymousUser")) {//未登录
            logger.info("开始从Cookie中查找购物车列表");
            //从cookie中提取购物车
            String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            if (cartListString == null || cartListString.equals("")) {
                cartListString = "[]";
            }
            cartList = JSON.parseArray(cartListString, Cart.class);
            cartService.saveCartListToRRedis(username, cartList);
        } else {//已登录
            logger.info("开始从Redis中查找购物车列表");
            cartList = cartService.findCartListFromRedis(username);
        }
        logger.info("结束查找购物车列表，结果：[{}]", JSON.toJSONString(cartList));
        return cartList;
    }
}