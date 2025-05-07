package com.multimart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String storeName;

    @Column(columnDefinition = "TEXT")
    private String storeDescription;

    @Embedded
    private Address storeAddress;

    private String logo;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    private String rejectionReason;

    private Double rating = 0.0;

    private Integer productCount = 0;

    private String specialty;

    private LocalDateTime joinedDate = LocalDateTime.now();

    private String contactEmail;

    private String contactPhone;

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
