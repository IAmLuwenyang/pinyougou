package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-06-27 下午 04:05
 */
@Service(timeout = 20000, retries = -1)
public class CartServiceImpl implements CartService {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据SKUID查询商品明细SKU的对象
        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在！");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态不合法！");
        }
        //2.根据SKU对象得到商家ID
        String sellerId = item.getSellerId();

        //3.根据商家ID在购物车列表中查询购物车对象
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart == null) {//4.购物车列表中【不存在】该商家的购物车
            //4.1 创建一个新的购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            List<TbOrderItem> orderItemList = new ArrayList<>();//创建购物车明细列表
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //4.2 将新的购物车对象添加到购物车列表中
            cartList.add(cart);
        } else {//5.购物车列表中【存在】该商家的购物车
            //判断该商品是否在该购物车的明细列表中存在
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {
                //5.1 如果不存在，创建新的购物车明细对象，并添加到该购物车的明细列表中
                cart.getOrderItemList().add(createOrderItem(item, num));
            } else {
                //5.2 如果存在，则在原有的商品数量上增加，并更新金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * orderItem.getNum()));
                //当明细数量≤0时，移除
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //当购物车明细数量为0时，移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        logger.info("结束添加购物车，结果：[{}]", JSON.toJSONString(cartList));
        return cartList;
    }

    /**
     * 根据商家ID在购物车列表中查询
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 根据SKUID在购物车明细列表中查询购物车明细对象
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建购物车明细对象
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();//创建新的购物车明细对象
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = new ArrayList<>();
        try {
            logger.info("开始从Redis中获取购物车列表");
            cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        } catch (Exception e) {
            logger.error("从Redis中获取购物车缓存异常,pinyougou-cart-service.CartServiceImpl.findCartListFromRedis() called with"
                + " username = {}", username, e);
        }
        logger.info("结束获取购物车列表，结果：[{}]", JSON.toJSONString(cartList));
        return cartList;
    }

    @Override
    public void saveCartListToRRedis(String username, List<Cart> cartList) {
        try {
            if (username.equals("") || username == null || CollectionUtils.isEmpty(cartList)) {
                logger.error("操作违规，原因[用户未登录/购物车列表为空]");
                return;
            }
            logger.info("开始向Redis中存储购物车列表");
            redisTemplate.boundHashOps("cartList").put(username, cartList);
        } catch (Exception e) {
            logger.error("存储购物车列表到Redis中异常，pinyougou-cart-service.CartServiceImpl.saveCartListToRRedis() called with "
                + "username = {}, cartList = [{}]", username, JSON.toJSONString(cartList));
        }
    }
}
