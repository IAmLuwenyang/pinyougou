package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {
	@Reference
	private BrandService brandService;
	
	@RequestMapping(value="/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	}
	
	@RequestMapping(value="/findPage")
	public PageResult findPage(int page, int size) {
		return brandService.findPage(page, size);
	}
	
	@RequestMapping(value="/add", method=RequestMethod.POST)
		public Result add(@RequestBody TbBrand brand) {
		try {
			brandService.add(brand);
			return new Result(true, "增加成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败!");
		}
	}
	
	@RequestMapping(value="/findOne")
	public TbBrand findOne(@RequestParam Long id) {
		return brandService.findOne(id);
	}
	
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		try {
			brandService.update(brand);
			return new Result(true, "更新成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "更新失败!");
		}
	}
	
	@RequestMapping("/delete")
	public Result delete(@RequestParam Long[] ids) {
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败!");
		}
	}
	
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand brand, int page, int size) {
		return brandService.findPage(brand, page, size);
	}
	
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectOptionList();
	}
}
