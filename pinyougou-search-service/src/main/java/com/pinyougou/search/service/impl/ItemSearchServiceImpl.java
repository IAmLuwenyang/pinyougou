package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

/**
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-05-30 下午 07:48
 */
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService{
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {
        /*Map map = new HashMap();
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows", page.getContent());
        return map;*/

        Map map = new HashMap();
        //空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

        //1.查询列表
        map.putAll(searchList(searchMap));

        //2.分组查询商品分类列表
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

        //3.查询品牌和规格列表
        String category = (String) searchMap.get("category");
        if(!"".equals(category)){
            Map brandAndSpecMap = searchBrandAndSpecList(category);
            map.putAll(brandAndSpecMap);
        }else {
            if(categoryList.size() > 0){
                Map brandAndSpecMap = searchBrandAndSpecList((String) categoryList.get(0));
                map.putAll(brandAndSpecMap);
            }
        }

        return map;
    }

    /**
     * 查询列表
     * @param searchMap：查询参数，使用Map类型封装，方便后期扩展。
     * @return
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap();
        //构建高亮选项对象
        HighlightQuery highlightQuery = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
        highlightOptions.setSimplePostfix("</em>");//后缀
        highlightQuery.setHighlightOptions(highlightOptions);

        //1.1 关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        highlightQuery.addCriteria(criteria);
        //1.2 按照商品分类category过滤
        if(!"".equals(searchMap.get("category")) ){
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //1.3 按品牌brand过滤
        if(!"".equals(searchMap.get("brand")) ){
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //1.4 按规格spec过滤
        if(searchMap.get("spec") != null){
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for(String key : specMap.keySet()){
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }
        //1.5 按价格过滤
        if(!"".equals(searchMap.get("price")) ){
            String priceStr = (String) searchMap.get("price");
            String[] priceArr = priceStr.split("-");
            if(!priceArr[0].equals("0")){//如果最低价格不等于0
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(priceArr[0]);
                filterQuery.addCriteria(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
            if(!priceArr[1].equals("*")){//如果最高价格不等于*
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(priceArr[1]);
                filterQuery.addCriteria(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }
        //1.6 分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo == null){
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize == null){
            pageSize = 20;
        }
        highlightQuery.setOffset((pageNo-1)*pageSize);//起始索引
        highlightQuery.setRows(pageSize);//每页记录数
        //1.7 按价格排序
        String sortValue = (String) searchMap.get("sort");//升序ASC，降序DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if(sortValue != null && !sortValue.equals("")){
            if(sortValue.equals("ASC")){
                Sort priceSort = new Sort(Sort.Direction.ASC, "item_"+sortField);
                highlightQuery.addSort(priceSort);
            }else if(sortValue.equals("DESC")){
                Sort priceSort = new Sort(Sort.Direction.DESC, "item_"+sortField);
                highlightQuery.addSort(priceSort);
            }
        }


        //************** 获取高亮结果集 *****************
        //返回高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);
        //高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {//遍历每条记录
            //获取高亮列表（高亮域的个数）
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();//获取每条记录中被高亮包装的域列表
            //List<String> sns = highlightList.get(0).getSnipplets();//每个域有可能存储多值
            if(highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0){
                TbItem item = entry.getEntity();
                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());
        map.put("totalPage", page.getTotalPages());//总页数
        map.put("total", page.getTotalElements());//总记录数
        return map;
    }

    /**
     * 分组查询：查询商品分类列表
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap){
        List list = new ArrayList();
        Query q = new SimpleQuery();
        Query query = new SimpleQuery("*:*");
        //关键字查询：相当于SQL中的where
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //分组查询：group by
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象，page可能包含多个分组条件而划分多个分组结果
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());//将分组的结构添加到返回值中
        }
        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 作用：查询品牌和规格列表
     * @param category : 商品分类名称。
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        //1.根据商品分类名称从缓存中获取模板ID；
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(templateId != null){
            //2.根据模板ID获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);
            //3.根据模板ID获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 删除Solr索引库
     * @param goodsIds
     */
    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
