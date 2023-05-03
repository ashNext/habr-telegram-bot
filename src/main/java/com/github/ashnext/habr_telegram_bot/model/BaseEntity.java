package com.github.ashnext.habr_telegram_bot.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;
import java.util.UUID;

@MappedSuperclass
@Getter
@EqualsAndHashCode
@ToString
public abstract class BaseEntity {

    @Id
    protected final UUID id = UUID.randomUUID();
}
