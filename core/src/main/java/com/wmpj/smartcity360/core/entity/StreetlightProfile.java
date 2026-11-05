package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name="streetlight_profiles")
public class StreetlightProfile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id",nullable=false,updatable=false) private Long id;
    @Column(name="profile_code",nullable=false,unique=true,length=50) private String profileCode;
    @Column(name="name",nullable=false,length=100) private String name;
    @Column(name="manufacturer",length=100) private String manufacturer;
    @Column(name="model",length=100) private String model;
    @Column(name="wattage",nullable=false,precision=8,scale=2) private BigDecimal wattage;
    @Enumerated(EnumType.STRING) @Column(name="light_type",nullable=false,length=30) private StreetlightType lightType;
    @Column(name="default_brightness",nullable=false) private short defaultBrightness=100;
    @Column(name="firmware_version",length=50) private String firmwareVersion;
    @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
    protected StreetlightProfile() {}
    public Long getId(){return id;} public String getProfileCode(){return profileCode;} public void setProfileCode(String v){profileCode=v;}
    public String getName(){return name;} public void setName(String v){name=v;} public String getManufacturer(){return manufacturer;} public void setManufacturer(String v){manufacturer=v;}
    public String getModel(){return model;} public void setModel(String v){model=v;} public BigDecimal getWattage(){return wattage;} public void setWattage(BigDecimal v){wattage=v;}
    public StreetlightType getLightType(){return lightType;} public void setLightType(StreetlightType v){lightType=v;} public short getDefaultBrightness(){return defaultBrightness;} public void setDefaultBrightness(short v){defaultBrightness=v;}
    public String getFirmwareVersion(){return firmwareVersion;} public void setFirmwareVersion(String v){firmwareVersion=v;} public OffsetDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(OffsetDateTime v){createdAt=v;}
}
