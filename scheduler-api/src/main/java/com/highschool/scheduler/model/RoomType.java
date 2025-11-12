package com.highschool.scheduler.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "room_types")
public class RoomType {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

}
