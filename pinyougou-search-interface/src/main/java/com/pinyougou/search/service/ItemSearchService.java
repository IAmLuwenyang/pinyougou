package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-05-30 下午 07:43
 */
public interface ItemSearchService {
    /**
     * 搜索方法
     * @param searchMap
     * @return
     */
    public Map search(Map searchMap);

    /**
     * 导入列表
     * @param list
     */
    public void importList(List list);

    /**
     * 在Solr索引库中删除商品类别
     * @param goodsIds
     */
    public void deleteByGoodsIds(List goodsIds);
}
