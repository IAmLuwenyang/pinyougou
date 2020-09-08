package com.pinyougou.sellergoods.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;

@RestController
public class UploadController {
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;	//获取配置文件中FILE_SERVER_URL的值
	
	@RequestMapping("/upload")
	public Result upload(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();	//获取文件名		
		String extName = originalFilename.substring(originalFilename.lastIndexOf(".")+1);	//得到扩展名
		try {
			util.FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
			String uploadFileId = client.uploadFile(file.getBytes(), extName);
			String url = FILE_SERVER_URL + uploadFileId;	//图片的完整地址
			return new Result(true, url);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "上传失败!");	
		}
	}
}
