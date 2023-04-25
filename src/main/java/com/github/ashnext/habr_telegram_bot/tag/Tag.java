package com.github.ashnext.habr_telegram_bot.tag;

import com.github.ashnext.habr_telegram_bot.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity()
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"}, name = "unq$tag$name")})
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
public class Tag extends BaseEntity {

    @Column(nullable = false)
    private final String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_group", nullable = false)
    private final TagGroup tagGroup;

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name=" + name +
                ", tagGroup=" + tagGroup +
                '}';
    }
}
