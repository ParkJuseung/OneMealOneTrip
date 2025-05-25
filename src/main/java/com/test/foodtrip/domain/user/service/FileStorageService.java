package com.test.foodtrip.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

/**
 * MultipartFile을 받아 로컬 디스크에 저장해 주는 서비스
 */
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        // 설정값을 받아 절대 경로로 변환 및 정규화
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            // 디렉터리가 없으면 생성
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("업로드 폴더 생성에 실패했습니다.", ex);
        }
    }

    /**
     * 파일을 저장하고, 저장된 파일명을 반환합니다.
     *
     * @param file 업로드된 MultipartFile
     * @return 저장된 파일명
     */
    public String storeFile(MultipartFile file) {
        // 원본 파일명에서 경로 정보 제거
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // 보안 체크: 부적절한 경로 포함 여부
            if (originalFilename.contains("..")) {
                throw new RuntimeException("파일명에 부적절한 경로 정보가 포함되어 있습니다: " + originalFilename);
            }

            // 저장할 대상 경로 결정
            Path targetLocation = this.fileStorageLocation.resolve(originalFilename);

            // 실제 파일 저장(덮어쓰기)
            file.transferTo(targetLocation.toFile());

            return originalFilename;
        } catch (IOException ex) {
            throw new RuntimeException("파일 저장에 실패했습니다: " + originalFilename, ex);
        }
    }


}
