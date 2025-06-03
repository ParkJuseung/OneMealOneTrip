package com.test.foodtrip.domain.user.service.impl;

import com.test.foodtrip.domain.user.dto.VisitorStatsDTO;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.entity.VisitorCount;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.domain.user.repository.VisitorCountRepository;
import com.test.foodtrip.domain.user.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VisitorServiceImpl implements VisitorService {

    private final VisitorCountRepository visitorCountRepository;

    private final UserRepository userRepository;

    @Override
    public VisitorStatsDTO getStats(Long pageOwnerId) {
        LocalDate today     = LocalDate.now();
        LocalDate weekSince = today.minusDays(6);
        LocalDate monthSince= today.minusDays(29);

        long todayCount = visitorCountRepository.countByUserIdAndVisitDateBetween(pageOwnerId, today, today);
        long weekCount  = visitorCountRepository.countByUserIdAndVisitDateGreaterThanEqual(pageOwnerId, weekSince);
        long monthCount = visitorCountRepository.countByUserIdAndVisitDateGreaterThanEqual(pageOwnerId, monthSince);
        long totalCount = visitorCountRepository.countByUserId(pageOwnerId);

        return VisitorStatsDTO.builder()
                .todayCount(todayCount)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .totalCount(totalCount)
                .build();

    }


    @Override
    @Transactional
    public void recordVisit(Long pageOwnerId, Long visitorUserId) {
        // 1) 비로그인 or 본인 페이지 방문이면 스킵
        if (visitorUserId == null || visitorUserId.equals(pageOwnerId)) return;

        // 2) 오늘 날짜 계산
        LocalDate today = LocalDate.now();

        // 3) 오늘 이미 한 번 기록이 남아 있는지 체크
        boolean already = visitorCountRepository
                .existsByUser_IdAndVisitorUser_IdAndVisitDateBetween(
                        pageOwnerId,
                        visitorUserId,
                        today,
                        today
                );
        if (already) {
            // 이미 오늘 방문했으므로 더 이상 저장하지 않음
            return;
        }

        // 4) 없었으면 User 엔티티 조회 및 새로운 VisitorCount 저장
        User pageOwner = userRepository.findById(pageOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("페이지 소유자 없음: " + pageOwnerId));
        User visitor   = userRepository.findById(visitorUserId)
                .orElseThrow(() -> new IllegalArgumentException("방문자 없음: " + visitorUserId));

        VisitorCount vc = VisitorCount.builder()
                .user(pageOwner)            // 관계 연관 설정
                .visitorUser(visitor)       // 방문자 설정
                .visitorCount(1)            // 기본 방문 횟수 1회
                .visitDate(today)
                .build();
        visitorCountRepository.save(vc);
    }

        





}
