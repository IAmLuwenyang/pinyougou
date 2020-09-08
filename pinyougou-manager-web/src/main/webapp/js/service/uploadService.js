app.service('uploadService', function($http){
	//上传文件的方法
	this.uploadFile = function(){
		var formData = new FormData();	//HTML5中的类，代表一个表单数据
		formData.append('file',file.files[0]);	//file：文件上传框的name
		
		return $http({
            method:'POST',
            url:"../upload.do",
            data: formData,
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        });		
	}
});