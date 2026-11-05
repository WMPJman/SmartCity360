package com.wmpj.smartcity360.core.entity;
import jakarta.persistence.*; import java.time.OffsetDateTime;
@Entity @Table(name="city_policies") public class CityPolicy {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id",nullable=false,updatable=false) private Long id;
 @Column(name="policy_code",nullable=false,unique=true,length=60) private String policyCode;
 @Column(name="name",nullable=false,length=150) private String name; @Column(name="description") private String description;
 @ManyToOne(fetch=FetchType.LAZY,optional=true) @JoinColumn(name="zone_id") private Zone zone;
 @Enumerated(EnumType.STRING) @Column(name="policy_type",nullable=false,length=30) private CityPolicyType policyType;
 @Column(name="dsl_expression",nullable=false) private String dslExpression;
 @Enumerated(EnumType.STRING) @Column(name="status",nullable=false,length=20) private CityPolicyStatus status=CityPolicyStatus.DRAFT;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="created_by",nullable=false) private User createdBy;
 @ManyToOne(fetch=FetchType.LAZY,optional=true) @JoinColumn(name="updated_by") private User updatedBy;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 protected CityPolicy(){}
    public CityPolicy(
            String policyCode,
            String name,
            String description,
            Zone zone,
            CityPolicyType policyType,
            String dslExpression,
            User createdBy,
            OffsetDateTime createdAt
    ) {
        this.policyCode = policyCode;
        this.name = name;
        this.description = description;
        this.zone = zone;
        this.policyType = policyType;
        this.dslExpression = dslExpression;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
 public Long getId(){return id;} public String getPolicyCode(){return policyCode;} public void setPolicyCode(String v){policyCode=v;} public String getName(){return name;} public void setName(String v){name=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public Zone getZone(){return zone;} public void setZone(Zone v){zone=v;} public CityPolicyType getPolicyType(){return policyType;} public void setPolicyType(CityPolicyType v){policyType=v;} public String getDslExpression(){return dslExpression;} public void setDslExpression(String v){dslExpression=v;} public CityPolicyStatus getStatus(){return status;} public void setStatus(CityPolicyStatus v){status=v;} public User getCreatedBy(){return createdBy;} public void setCreatedBy(User v){createdBy=v;} public User getUpdatedBy(){return updatedBy;} public void setUpdatedBy(User v){updatedBy=v;} public OffsetDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(OffsetDateTime v){createdAt=v;} public OffsetDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(OffsetDateTime v){updatedAt=v;}
}
