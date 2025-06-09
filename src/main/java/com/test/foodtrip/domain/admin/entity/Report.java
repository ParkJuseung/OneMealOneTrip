package com.test.foodtrip.domain.admin.entity;

import com.test.foodtrip.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "REPORT")
@Getter @Setter
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq")
    @SequenceGenerator(name = "report_seq", sequenceName = "report_seq", allocationSize = 1)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id")
    private User reported;

    @Column(name = "report_type", nullable = false, length = 30)
    private String reportType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reason", nullable = false, length = 30)
    private String reason;

    @Column(name = "detail", length = 1000)
    private String detail;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id")
    private User processor;

    // 🆕 public 생성자 추가 (서비스에서 사용하기 위해)
    public Report() {
        // 기본 생성자
    }

    // 🆕 편의 생성자 추가
    public Report(User reporter, User reported, String reportType, Long targetId, String reason, String detail) {
        this.reporter = reporter;
        this.reported = reported;
        this.reportType = reportType;
        this.targetId = targetId;
        this.reason = reason;
        this.detail = detail;
        this.status = "PENDING";
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}