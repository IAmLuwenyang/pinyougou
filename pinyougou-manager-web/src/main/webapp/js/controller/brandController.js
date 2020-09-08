//建立brandController控制器
app.controller("brandController", function($scope, $controller, brandService){
	$controller('baseController', {$scope:$scope});
	
	//查询所有品牌列表
	$scope.findAll = function(){
		/* $http.get('../brand/findAll.do').success(function(response){
			$scope.brandList = response;
		}); */
		
		brandService.findAll().success(function(response){
			$scope.brandList = response;
		});
	}
	
	//这部分代码通过继承获得
	/*//分页控件配置 
	$scope.paginationConf = {
			 currentPage: 1,
			 totalItems: 10,
			 itemsPerPage: 10,
			 perPageOptions: [10, 20, 30, 40, 50],
			 onChange: function(){
		        	 $scope.reloadList();
			 }
	}; 
	
	//刷新页面
	$scope.reloadList = function(){
		//$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
		$scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
	}*/
	
	//分页方法
	$scope.findPage = function(page, size){
		brandService.findPage(page, size).success(
			function(response){
				$scope.brandList = response.rows;	//显示当前页的数据
				$scope.paginationConf.totalItems = response.total;	//更新总记录数
			}		
		);
	}
	
	//新增方法 $scope.entity被当作消息体发送给服务器，在POST请求中使用
	$scope.save = function() {
		/* var methodName = "add";	//方法名
		if($scope.entity.id != null){	//添加品牌时没有ID
			methodName = "update";
		}
		$http.post('../brand/' + methodName + '.do', $scope.entity).success(
			function(response){
				if(response.success){
					$scope.reloadList();	//刷新
				}else{
					alert(response.message);
				}
			}		
		); */
		
		var object = null;
		if($scope.entity.id != null){	//添加品牌时没有ID
			object = brandService.update($scope.entity);
		}else{
			object = brandService.add($scope.entity);
		}
		object.success(
			function(response){
				if(response.success){
					$scope.reloadList();	//刷新
				}else{
					alert(response.message);
				}
			}		
		); 
	}
	
	//查询实体
	$scope.findOne = function(id){
		brandService.findOne(id).success(
			function(response){
				$scope.entity = response;
			}		
		);
	}
	
	//这部分代码通过继承获得
	/*$scope.selectIds = [];	//用户勾选的ID集合
	
	$scope.updateSelection = function($event, id){
		if($event.target.checked){
			$scope.selectIds.push(id);	//push向集合添加元素
		}else{
			var index = $scope.selectIds.indexOf(id);	//查找 值 的下标
			//delete $scope.selectIds[index];	//delete删除之后数组长度不变，只是被删除元素被置为undefined了
			$scope.selectIds.splice(index, 1);	//参数1：移除的元素下标；参数2：移除的元素个数
		}
	}*/
	
	//删除复选框中用户勾选的数据
	$scope.dele = function(){
		brandService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					$scope.reloadList();	//刷新
				}else{
					alert(response.message);
				}
			}		
		);
	}
	
	//初始化
	$scope.searchEntity = {};
	//条件查询
	$scope.search = function(page, size){
		brandService.search(page, size, $scope.searchEntity).success(
			function(response){
				$scope.brandList = response.rows;	//显示当前页的数据
				$scope.paginationConf.totalItems = response.total;	//更新总记录数
			}		
		);
	}
	
});