package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity @Table(name="streetlights")
public class Streetlight {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id",nullable=false,updatable=false) private Long id;
    @Column(name="light_code",nullable=false,unique=true,length=60) private String lightCode;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="profile_id",nullable=false) private StreetlightProfile profile;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="zone_id",nullable=false) private Zone zone;
    @Column(name="latitude",precision=9,scale=6) private BigDecimal latitude;
    @Column(name="longitude",precision=9,scale=6) private BigDecimal longitude;
    @Column(name="is_on",nullable=false) private boolean on;
    @Column(name="brightness",nullable=false) private short brightness=100;
    @Enumerated(EnumType.STRING) @Column(name="operational_status",nullable=false,length=20) private StreetlightOperationalStatus operationalStatus=StreetlightOperationalStatus.ACTIVE;
    @Column(name="installed_at") private LocalDate installedAt;
    @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
    protected Streetlight() {}
    public Long getId(){return id;} public String getLightCode(){return lightCode;} public void setLightCode(String v){lightCode=v;}
    public StreetlightProfile getProfile(){return profile;} public void setProfile(StreetlightProfile v){profile=v;} public Zone getZone(){return zone;} public void setZone(Zone v){zone=v;}
    public BigDecimal getLatitude(){return latitude;} public void setLatitude(BigDecimal v){latitude=v;} public BigDecimal getLongitude(){return longitude;} public void setLongitude(BigDecimal v){longitude=v;}
    public boolean isOn(){return on;} public void setOn(boolean v){on=v;} public short getBrightness(){return brightness;} public void setBrightness(short v){brightness=v;}
    public StreetlightOperationalStatus getOperationalStatus(){return operationalStatus;} public void setOperationalStatus(StreetlightOperationalStatus v){operationalStatus=v;}
    public LocalDate getInstalledAt(){return installedAt;} public void setInstalledAt(LocalDate v){installedAt=v;} public OffsetDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(OffsetDateTime v){updatedAt=v;}
}
