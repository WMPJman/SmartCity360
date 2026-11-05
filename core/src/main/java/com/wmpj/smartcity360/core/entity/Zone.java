package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "zones")
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parent_zone_id", nullable = true)
    private Zone parent;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "zone_code", nullable = false, unique = true, length = 50)
    private String zoneCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false, length = 20)
    private ZoneType zoneType;

    @Enumerated(EnumType.STRING)
    @Column(name = "power_status", nullable = false, length = 20)
    private PowerStatus powerStatus;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Zone> children = new ArrayList<>();

    protected Zone() {
    }

    public Zone(
            String name,
            String zoneCode,
            ZoneType zoneType,
            PowerStatus powerStatus,
            Zone parent,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.name = requireText(name, "name");
        this.zoneCode = requireText(zoneCode, "zoneCode");
        this.zoneType = Objects.requireNonNull(zoneType, "zoneType");
        this.powerStatus = Objects.requireNonNull(powerStatus, "powerStatus");
        this.parent = parent;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public Long getId() {
        return id;
    }

    public Zone getParent() {
        return parent;
    }

    public void setParent(Zone parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public void setZoneType(ZoneType zoneType) {
        this.zoneType = zoneType;
    }

    public PowerStatus getPowerStatus() {
        return powerStatus;
    }

    public void setPowerStatus(PowerStatus powerStatus) {
        this.powerStatus = powerStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Zone> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
