package com.github.ashnext.habr_telegram_bot.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@EqualsAndHashCode
@ToString
abstract class BaseEntity {

    @Id
    @GeneratedValue
    protected UUID id;
}
