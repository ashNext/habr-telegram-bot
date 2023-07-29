package com.github.ashnext.habr_telegram_bot.bookmark;

import com.github.ashnext.habr_telegram_bot.model.BaseEntity;
import com.github.ashnext.habr_telegram_bot.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_url"}, name = "unq$bookmark$user_id_post_url"))
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@Setter
public class Bookmark extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk$bookmark$id_user_id"))
    private final User user;

    @Column(name = "post_url", nullable = false)
    private final String postUrl;

    @Column(name = "post_title", nullable = false)
    private final String postTitle;

    @Override
    public String toString() {
        return "Bookmark{" +
                "id=" + id +
                ", postUrl=" + postUrl +
                ", postTitle=" + postTitle +
                '}';
    }
}
