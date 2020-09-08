package com.pinyougou.sellergoods.service.impl;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.mysql.fabric.xmlrpc.base.Data;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");   //状态0：未审核
		goodsMapper.insert(goods.getGoods());	//插入商品的基本信息	
		
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());	//将商品基本表的ID设置给商品扩展表
		goodsDescMapper.insert(goods.getGoodsDesc());	//插入商品扩展数据
		
		/*if("1".equals(goods.getGoods().getIsEnableSpec())) {
			for(TbItem item : goods.getItemList()) {
				//构建标题 SPU名称+规格选项值
				String title = goods.getGoods().getGoodsName();	//SPU名称
				Map<String, Object> map = JSON.parseObject(item.getSpec());	//将字符串JSON串转换成JSON对象
				for(String key : map.keySet()) {
					title += " " + map.get(key);
				}
				item.setTitle(title);
				
				setItemValues(item, goods);
				
				itemMapper.insert(item);
			}
		}else {	//没有启用规格
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());	//标题
			item.setPrice(goods.getGoods().getPrice());	//价格
			item.setNum(99999);	//库存数量
			item.setStatus("1");	//状态
			item.setIsDefault("1");	//是否默认
			item.setSpec("{}");
			
			setItemValues(item, goods);
			
			itemMapper.insert(item);
		}		*/
		saveItemList(goods); //插入SKU的商品数据
	}
	
	private void setItemValues(TbItem item, Goods goods) {
		//商品分类：三级分类ID
		item.setCategoryid(goods.getGoods().getCategory3Id());
		item.setCreateTime(new Date());	//创建日期
		item.setUpdateTime(new Date());	//更新日期
		item.setGoodsId(goods.getGoods().getId());	//商品ID
		item.setSellerId(goods.getGoods().getSellerId());	//商家ID
		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//商家名称(店铺名称)
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		//设置图片路径
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if(imageList.size() > 0) {
			item.setImage((String)imageList.get(0).get("url"));
		}	
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){	
		goods.getGoods().setAuditStatus("0");//设置未申请状态:如果是经过修改的商品，需要重新设置状态
		//更新基本表数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//更新扩展表数据
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//删除原有的SKU列表数据
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		//插入新的SKU列表数据
		saveItemList(goods);
	}	
	
	//插入SKU列表数据
	private void saveItemList(Goods goods) {
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			for(TbItem item : goods.getItemList()) {
				//构建标题 SPU名称+规格选项值
				String title = goods.getGoods().getGoodsName();	//SPU名称
				Map<String, Object> map = JSON.parseObject(item.getSpec());	//将字符串JSON串转换成JSON对象
				for(String key : map.keySet()) {
					title += " " + map.get(key);
				}
				item.setTitle(title);
				
				setItemValues(item, goods);
				
				itemMapper.insert(item);
			}
		}else {	//没有启用规格
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());	//标题
			item.setPrice(goods.getGoods().getPrice());	//价格
			item.setNum(99999);	//库存数量
			item.setStatus("1");	//状态
			item.setIsDefault("1");	//是否默认
			item.setSpec("{}");
			
			setItemValues(item, goods);
			
			itemMapper.insert(item);
		}
	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods =new Goods();
		//商品基本表
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//商品扩展表
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		
		//读取SKU列表
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> list = itemMapper.selectByExample(example);
		goods.setItemList(list);
		
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//goodsMapper.deleteByPrimaryKey(id);  //物理删除
			//逻辑删除
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");  //表示逻辑删除
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}
	
	
	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();//指定is_delete字段为空
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for(Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	/**
	 * 作用：根据SPU的ID集合查询SKU列表
	 * @param goodsIds
	 * @param status
	 * @return
	 */
	@Override
	public List<TbItem> findItemListByGoodsIdListAndStatus(Long[] goodsIds, String status) {
		//		List<TbItem> list = new ArrayList<>();
//		for (Long goodsId:goodsIds) {
//			TbItemExample example = new TbItemExample();
//			TbItemExample.Criteria criteria = example.createCriteria();
//			criteria.andIdEqualTo(goodsId);
//			criteria.andStatusEqualTo(status);//"1"已审核
//			list.addAll(itemMapper.selectByExample(example));
//		}
//		return list;

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status);
		criteria.andIdIn(Arrays.asList(goodsIds));//代替循环查询
		return itemMapper.selectByExample(example);
	}
}
