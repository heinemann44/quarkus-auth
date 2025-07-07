package br.com.user.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(length = 30, nullable = false, unique = true)
    public String username;

    @Column(length = 254, nullable = false, unique = true)
    public String email;

    @Column(length = 60, nullable = false)
    public String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    @PreRemove
    public void updateTimestamp() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        this.updatedAt = LocalDateTime.now();
    }

}