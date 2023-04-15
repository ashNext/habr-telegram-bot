package com.github.ashnext.habr_telegram_bot.user;

import com.github.ashnext.habr_telegram_bot.model.BaseEntity;
import com.github.ashnext.habr_telegram_bot.tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(
        name = "`user`",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"telegram_user_id"}, name = "unq$user$telegram_user_id"),
                @UniqueConstraint(columnNames = {"telegram_chat_id"}, name = "unq$user$telegram_chat_id")
        }
)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {

    @Column(name = "telegram_user_id", nullable = false)
    private final Long telegramUserId;

    @Column(name = "telegram_chat_id", nullable = false)
    private final Integer telegramChatId;

    @Column(nullable = false, columnDefinition = "bool default true")
    private boolean active = true;

    @Column(nullable = false, columnDefinition = "bool default true")
    private boolean subscription = true;

    @Column(name = "tags")
    @ElementCollection(targetClass = Tag.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "user_tags", joinColumns = @JoinColumn(name = "user_id"))
    @OrderBy("name ASC")
    private List<Tag> tags;

    public User(Long telegramUserId, Integer telegramChatId) {
        this.telegramUserId = telegramUserId;
        this.telegramChatId = telegramChatId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", telegramUserId=" + telegramUserId +
                ", telegramChatId=" + telegramChatId +
                ", active=" + active +
                ", subscription=" + subscription +
                '}';
    }
}
