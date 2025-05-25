package com.test.foodtrip.domain.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadService {

    // ✅ 현재 실행 위치 기준으로 uploads 폴더 설정
    private final String ROOT_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";

    // 파일 저장
    public String saveFile(MultipartFile file, String folder) {
        String uploadDir = ROOT_UPLOAD_DIR + "/" + folder;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 하위 폴더 포함 생성
            System.out.println("📁 폴더 생성됨: " + dir.getAbsolutePath());
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File targetFile = new File(uploadDir, fileName);

        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }

        // 클라이언트가 접근할 수 있는 상대경로로 반환
        return "/uploads/" + folder + "/" + fileName;
    }

    // 이미지 파일인지 검사
    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
               (contentType.startsWith("image/jpeg") || contentType.startsWith("image/png"));
    }

    // [S3 전환 시점]
    // amazonS3.putObject(bucketName, key, file.getInputStream(), metadata);
    // return amazonS3.getUrl(bucketName, key).toString();

}
