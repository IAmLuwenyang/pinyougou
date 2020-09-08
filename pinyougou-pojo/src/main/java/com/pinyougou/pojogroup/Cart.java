package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbOrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author Mr.Lu
 * @company HUST&华中科技大学
 * @create 2020-06-27 下午 03:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Cart implements Serializable {

    private static final long serialVersionUID = -3200414670645810610L;
    private String sellerId;//商家ID

    private String sellerName;//商家名称

    private List<TbOrderItem> orderItemList;//购物车明细集合
}
