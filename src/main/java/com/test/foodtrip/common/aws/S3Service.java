package com.test.foodtrip.common.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return upload(uploadFile, dirName);
    }

    private String upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + changedImageName(uploadFile.getName());

        log.info("📦 S3Service.upload(File) 호출됨");
        log.info("📝 디렉토리명: {}", dirName);
        log.info("📝 업로드할 파일명 (S3 경로): {}", fileName);
        log.info("📂 로컬 파일 경로: {}", uploadFile.getAbsolutePath());

        String uploadImageUrl = putS3(uploadFile, fileName);

        log.info("✅ S3 업로드 완료 - 접근 URL: {}", uploadImageUrl);

        removeNewFile(uploadFile);
        log.info("🧹 로컬 임시 파일 삭제 완료: {}", uploadFile.getName());

        return uploadImageUrl;
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    public void deleteFile(String fileName) {
        try {
            amazonS3Client.deleteObject(bucket, fileName);
            log.info("S3 파일 삭제 완료: {}", fileName);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("S3 파일 삭제에 실패했습니다.", e);
        }
    }

    public String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        if (url.contains("/posts/")) {
            return url.substring(url.indexOf("/posts/") + 1);
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(file.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    private String changedImageName(String originName) {
        String random = UUID.randomUUID().toString();
        return random + originName;
    }
}
