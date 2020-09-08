package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * @author 卢文扬
 *
 */
public interface BrandService {
	public List<TbBrand> findAll();
	
	/**
	 * 品牌分页
	 * @param pageNum：当前页码
	 * @param pageSize：每页记录数
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	/**
	 * 增加品牌
	 * @param brand
	 */
	public void add(TbBrand brand);
	
	/**
	 * 根据品牌ID查询实体
	 * @param id：品牌ID
	 * @return
	 */
	public TbBrand findOne(Long id);
	
	/**
	 * 修改品牌实体
	 * @param id
	 */
	public void update(TbBrand brand);
	
	/**
	 * 删除品牌实体
	 * @param ids
	 */
	public void delete(Long[] ids);
	
	/**
	 * 品牌分页-条件查询
	 * @param pageNum：当前页码
	 * @param pageSize：每页记录数
	 * @param brand：查询条件
	 * @return
	 */
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize);
	
	/**
	 * 返回关联品牌的下拉列表数据
	 * @return
	 */
	public List<Map> selectOptionList();
}
