app.controller('baseController', function($scope){
	//分页控件配置
	//onChange：当页码变更后自动触发的方法
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
	}
	
	$scope.selectIds = [];	//用户勾选的ID集合
	
	$scope.updateSelection = function($event, id){
		if($event.target.checked){
			$scope.selectIds.push(id);	//push向集合添加元素
		}else{
			var index = $scope.selectIds.indexOf(id);	//查找值的下标
			//delete $scope.selectIds[index];	//delete删除之后数组长度不变，只是被删除元素被置为undefined了
			$scope.selectIds.splice(index, 1);	//参数1：移除的元素下标；参数2：移除的元素个数
		}
	}
	
	//
	$scope.jsonToString = function(jsonString, key){
		var json = JSON.parse(jsonString);
		var value = "";
		for(var i=0; i<json.length; i++){
			if(i>0){
				value += ",";
			}
			value += json[i][key];
		}
		return value;
	}
});