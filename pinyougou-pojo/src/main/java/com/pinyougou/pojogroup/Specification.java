package com.pinyougou.pojogroup;

import java.io.Serializable;
import java.util.List;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;

/**
 * 规格组合实体类
 * @author 卢文扬
 *
 */
public class Specification implements Serializable {
	private TbSpecification tbSpecification;
	private List<TbSpecificationOption> specificationOptionList;
	
	public TbSpecification getTbSpecification() {
		return tbSpecification;
	}
	public void setTbSpecification(TbSpecification tbSpecification) {
		this.tbSpecification = tbSpecification;
	}
	public List<TbSpecificationOption> getSpecificationOptionList() {
		return specificationOptionList;
	}
	public void setSpecificationOptionList(List<TbSpecificationOption> specificationOptionList) {
		this.specificationOptionList = specificationOptionList;
	}
	
	
}
