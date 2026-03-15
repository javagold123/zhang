package com.itlichao.backendlabor;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendLaborApplicationTests {

    @Test
    void contextLoads() {
        try {
            // 1. 创建 MinIO 客户端
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("http://192.168.88.130:9000")
                    .credentials("IvQ5NcCtz3poHkLpSWou", "YZgsirNwCjxxZOG8vQPmgOZhlA3T5ubd1qbuzvKD")
                    .build();

            // 2. 本地图片路径
            String localFilePath = "E:/test.png";

            // 3. 上传到桶中的目标路径（可以不存在，MinIO会自动创建前缀）
            String objectName = "public/test.jpg";

            // 4. 上传
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("laboratory")
                            .object(objectName)
                            .filename(localFilePath)
                            .build()
            );

            System.out.println("图片上传成功: " + objectName);

        } catch (MinioException e) {
            System.err.println("上传失败: " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
