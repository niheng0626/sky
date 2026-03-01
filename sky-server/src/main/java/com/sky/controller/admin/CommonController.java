package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.MinioUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Value("${sky.minio.endpoint}")
    private String minioBaseUrl;
    // 文件上传
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file);

        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 原始文件名
            String originalFilename = file.getOriginalFilename();

            // 截取文件名后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

            // UUID构建唯一文件名
            String objectName = UUID.randomUUID().toString() + suffix;

            // 使用MinioUtil上传文件
            String filePath = MinioUtil.upload(file.getBytes(), objectName);

            // 构建完整URL（根据实际情况调整）
            // 这里返回相对路径，前端可以根据需要拼接完整URL
            String fullFileUrl = minioBaseUrl + filePath;
            return Result.success(fullFileUrl);

        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}