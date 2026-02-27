package com.sky.utils;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
@Slf4j
public class MinioUtil {

    private static MinioClient minioClient;

    // 桶名称固定为 "sky"
    private static final String BUCKET_NAME = "sky";

    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        MinioUtil.minioClient = minioClient;
        // 初始化时检查桶是否存在，不存在则创建
        initBucket();
    }

    /**
     * 初始化存储桶
     */
    private static void initBucket() {
        try {
            // 检查存储桶是否存在
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(BUCKET_NAME).build()
            );

            if (!bucketExists) {
                // 创建存储桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(BUCKET_NAME).build()
                );
                log.info("MinIO存储桶 '{}' 创建成功", BUCKET_NAME);
            } else {
                log.info("MinIO存储桶 '{}' 已存在", BUCKET_NAME);
            }

        } catch (Exception e) {
            log.error("初始化MinIO存储桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("MinIO存储桶初始化失败", e);
        }
    }

    /**
     * 上传文件到MinIO
     *
     * @param bytes 文件字节数组
     * @param objectName 存储的对象名称（包含后缀）
     * @return 文件访问URL
     */
    public static String upload(byte[] bytes, String objectName) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("文件内容不能为空");
        }

        if (objectName == null || objectName.trim().isEmpty()) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            // 上传文件到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(inputStream, bytes.length, -1)
                            .contentType(getContentType(objectName))
                            .build()
            );

            // 构建文件访问URL（这里返回相对路径，Controller层可以根据需要拼接完整URL）
            String fileUrl = String.format("/%s/%s", BUCKET_NAME, objectName);
            log.info("文件上传成功: {}", fileUrl);

            return fileUrl;

        } catch (Exception e) {
            log.error("文件上传到MinIO失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 上传MultipartFile文件（扩展方法，方便直接上传MultipartFile）
     *
     * @param file MultipartFile对象
     * @param objectName 存储的对象名称（包含后缀）
     * @return 文件访问URL
     */
    public static String upload(MultipartFile file, String objectName) throws IOException {
        return upload(file.getBytes(), objectName);
    }

    /**
     * 根据文件后缀获取Content-Type
     */
    private static String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 获取MinIO客户端实例（供其他方法使用）
     */
    public static MinioClient getMinioClient() {
        return minioClient;
    }

    /**
     * 获取桶名称
     */
    public static String getBucketName() {
        return BUCKET_NAME;
    }
}