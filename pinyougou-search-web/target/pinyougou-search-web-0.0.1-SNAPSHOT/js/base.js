//建立模块时 引入pagination模块
var app = angular.module("pinyougou", []);	//定义pinyougou模块

//自定义过滤器
app.filter('trustHtml', ['$sce',function ($sce) {
    return function (data) {//data是被过滤的内容
        return $sce.trustAsHtml(data);//返回过滤后的内容，信任HTML的转换
    }
}]);