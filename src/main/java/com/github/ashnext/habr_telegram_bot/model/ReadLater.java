package com.github.ashnext.habr_telegram_bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "postUrl"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReadLater extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String postUrl;

    @Column(nullable = false)
    private String postTitle;

    @Override
    public String toString() {
        return "ReadLater{" +
                "id=" + id +
                ", postUrl=" + postUrl +
                ", postTitle=" + postTitle +
                '}';
    }
}
