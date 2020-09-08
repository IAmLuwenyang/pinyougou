package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-06-27 下午 07:38
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        try {
            //1.从cookie中提取购物车
            List<Cart> cartList = findCartList();
            //2.调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //3.将新的购物车存入cookie
            String cartListString = JSON.toJSONString(cartList);
            CookieUtil.setCookie(request, response,"cartList", cartListString, 3600*24, "UTF-8");
            return new Result(true, "存入购物车成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "存入购物车失败！");
        }
    }

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //当前登录账号
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if(name.equals("anonymousUser")){//未登录

        }else{//已登录

        }
        //从cookie中提取购物车
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")){
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        return cartList_cookie;
    }
}