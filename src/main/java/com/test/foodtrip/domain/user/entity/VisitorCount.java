package com.test.foodtrip.domain.user.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "VISITOR_COUNT")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VisitorCount {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visitor_count_seq")
    @SequenceGenerator(name = "visitor_count_seq", sequenceName = "visitor_count_seq", allocationSize = 1)
    @Column(name = "visitor_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_user_id")
    private User visitorUser;

    @Column(name = "visitor_count")
    private Integer visitorCount;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;
}
