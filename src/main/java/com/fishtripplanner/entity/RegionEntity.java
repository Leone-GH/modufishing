package com.fishtripplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Entity
@Table(name = "region")
public class RegionEntity {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private RegionEntity parent;

    @OneToMany(mappedBy = "parent")
    private List<RegionEntity> children = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegionEntity)) return false;
        RegionEntity other = (RegionEntity) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}