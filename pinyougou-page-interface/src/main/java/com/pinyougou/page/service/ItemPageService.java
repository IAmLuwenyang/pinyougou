package com.pinyougou.page.service;

/**
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-06-03 上午 11:12
 */
public interface ItemPageService {
    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详情页
     * @param ids
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}
