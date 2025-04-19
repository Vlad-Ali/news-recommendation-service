package org.hsse.news.database.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hsse.news.database.userrequest.model.UserRequestDto;

import java.time.Instant;

@Entity
@Getter
@Table(name = "user_requests", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "url"})})
public class UserRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id", updatable = false, nullable = false)
    private Long requestId;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "url")
    @NotNull
    private String url;

    @Column(name = "request_time")
    @NotNull
    private Instant requestTime;

    protected UserRequestEntity() {}

    public UserRequestEntity(final @NotNull UserEntity user, final @NotNull String url, final @NotNull Instant requestTime){
        this.user = user;
        this.url = url;
        this.requestTime = requestTime;
    }

    public UserRequestDto toDto(){
        return new UserRequestDto(requestId, user.getId(), url, requestTime);
    }

    @Override
    public boolean equals(final Object o){
        if (this == o){
            return true;
        }
        if (o instanceof UserRequestEntity userRequestEntity){
            return requestId!=null && requestId.equals(userRequestEntity.requestId);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return UserRequestEntity.class.hashCode();
    }

}
