app.controller('searchController', function ($scope, $location, searchService) {
    //定义搜索对象的结构，category：商品分类
    $scope.searchMap = {'keywords':'', 'category':'', 'brand':'', 'spec':{},'price':'', 'pageNo':1, 'pageSize':20, 'sort':'', 'sortField':'' };

    $scope.search=function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
            buildPageLabel();
        });
    }

    buildPageLabel=function(){
        //构建分页栏
        $scope.pageLabel = [];
        var firstPage = 1;
        var lastPage = $scope.resultMap.totalPage;
        if($scope.resultMap.totalPage > 5){
            if($scope.searchMap.pageNo <= 3){
                lastPage = 5;
            }else if($scope.searchMap.pageNo >= $scope.resultMap.totalPage-2){
                firstPage = $scope.resultMap.totalPage-4;
            }else{
                firstPage = $scope.searchMap.pageNo-2;
                lastPage = $scope.searchMap.pageNo+2;
            }
        }
        for(var i=firstPage; i<=lastPage; i++){
            $scope.pageLabel.push(i);
        }
    }

    //添加搜索项，改变searchMap的值
    $scope.addSearchItem=function (key, value) {
        if(key=='category' || key=='brand' || key=='price'){
            $scope.searchMap[key]=value;
        }else{//规格
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();
    }

    //撤销搜索选项
    $scope.removeSearchItem=function (key) {
        if(key=='category' || key=='brand' || key=='price'){
            $scope.searchMap[key]="";
        }else{//规格
            delete $scope.searchMap.spec[key];//删除
        }
        $scope.search();
    }

    //分页查询
    $scope.queryByPage=function (pageNo) {
        if(pageNo<1 || pageNo>$scope.resultMap.totalPage){
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //判断当前页是否为第一页
    $scope.isTopPage=function () {
        if($scope.searchMap.pageNo == 1){
            return true;
        }else {
            return false;
        }
    }
    //判断当前页是否为最后一页
    $scope.isEndPage=function () {
        if($scope.searchMap.pageNo == $scope.resultMap.totalPage){
            return true;
        }else {
            return false;
        }
    }

    //排序查询
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    //判断搜索关键字是否是品牌
    $scope.keywordsIsBrand=function () {
        for (var i=0; i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }

    $scope.loadkeywords=function(){
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }
});