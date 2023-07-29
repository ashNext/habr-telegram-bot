package com.github.ashnext.habr_telegram_bot.model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@MappedSuperclass
@Getter
@EqualsAndHashCode
@ToString
public abstract class BaseEntity {

    @Id
    protected final UUID id = UUID.randomUUID();
}
