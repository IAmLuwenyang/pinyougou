package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-06-04 下午 10:49
 */
@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();//JSON字符串
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            //导入Solr索引库
            itemSearchService.importList(itemList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
