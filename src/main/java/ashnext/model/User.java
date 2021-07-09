package ashnext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "`user`")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private Long telegramUserId;

    @Column(nullable = false)
    private Integer telegramChatId;

    @Column(nullable = false, columnDefinition = "bool default true")
    private boolean active;

    @Column(nullable = false, columnDefinition = "bool default false")
    private boolean subscription;

    @CollectionTable(name = "user_tags", joinColumns = @JoinColumn(name = "user_id")
            ,uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "tags_name"}, name = "user_tags_unique_idx")}
    )
    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "tag")
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
                ", active=" + active +
                ", subscription=" + subscription +
                '}';
    }
}
