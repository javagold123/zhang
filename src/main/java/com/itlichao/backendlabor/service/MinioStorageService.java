package com.itlichao.backendlabor.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.public-prefix}")
    private String publicPrefix;

    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadImage(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        String ext = getExtension(file.getOriginalFilename());
        String date = LocalDate.now().toString();
        String safeCategory = (category == null || category.isBlank()) ? "misc" : category.trim();
        String objectName = publicPrefix + "/" + safeCategory + "/" + date + "/" + UUID.randomUUID() + ext;
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(in, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("上传失败: " + e.getMessage());
        }
        return normalizeUrl(endpoint) + "/" + bucket + "/" + objectName;
    }

    public void ensureBucketExists() {
        try {
            StatObjectResponse ignore = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object("/").build()
            );
        } catch (ErrorResponseException e) {
            // ignore
        } catch (Exception e) {
            // ignore
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) return ".jpg";
        String name = filename.trim();
        int idx = name.lastIndexOf('.');
        if (idx < 0) return ".jpg";
        return name.substring(idx).toLowerCase();
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
