package com.github.ashnext.habr_telegram_bot.user;

import com.github.ashnext.habr_telegram_bot.hub.Hub;
import com.github.ashnext.habr_telegram_bot.model.BaseEntity;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.List;

@Entity
@TypeDefs({
        @TypeDef(name = "list-array", typeClass = ListArrayType.class)
})
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
    private final Long telegramChatId;

    @Column(nullable = false, columnDefinition = "bool default true")
    private boolean active = true;

    @Column(nullable = false, columnDefinition = "bool default true")
    private boolean subscription = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_hub",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "fk$user_hub$id_user_id")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "hub_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "fk$user_hub$id_hub_id")
            ),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"user_id", "hub_id"},
                    name = "unq$user_hub$user_id_hub_id"
            )
    )
    private List<Hub> hubs;

    @Type(type = "list-array")
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    public User(Long telegramUserId, Long telegramChatId) {
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
                ", tags=" + tags +
                '}';
    }
}
