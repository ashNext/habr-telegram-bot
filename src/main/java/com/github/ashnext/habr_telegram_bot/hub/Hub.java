package com.github.ashnext.habr_telegram_bot.hub;

import com.github.ashnext.habr_telegram_bot.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity()
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"}, name = "unq$hub$name")})
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Setter
public class Hub extends BaseEntity {

    @Column(nullable = false)
    private final String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "hub_group", nullable = false)
    private final HubGroup hubGroup;

    @Override
    public String toString() {
        return "Hub{" +
                "id=" + id +
                ", name=" + name +
                ", hubGroup=" + hubGroup +
                '}';
    }
}
