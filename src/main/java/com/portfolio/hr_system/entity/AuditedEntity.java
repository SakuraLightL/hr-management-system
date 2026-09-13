package com.portfolio.hr_system.entity;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

@Getter @MappedSuperclass @EntityListeners(AuditingEntityListener.class)
public abstract class AuditedEntity {
    @CreatedDate @Column(updatable = false) private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
    @CreatedBy @Column(updatable = false) private String createdBy;
    @LastModifiedBy private String updatedBy;
}
