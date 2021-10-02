package study.datajpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter @Setter
public class JpaBaseEntity {
    @Column(updatable = false)
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    @PrePersist
    public void prePersist(){   // 최초등록
        LocalDateTime now = LocalDateTime.now();
        createDate = now;
        updateDate = now;
    }
    @PreUpdate
    public void preUpdate(){   // 업데이트
        updateDate = LocalDateTime.now();
    }
}
