package ashnext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "`user`")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private Long telegramUserId;

    @Column(nullable = false, columnDefinition = "bool default true")
    private boolean active;

    @Column(nullable = false, columnDefinition = "bool default false")
    private boolean subscription;

    public User(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
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
