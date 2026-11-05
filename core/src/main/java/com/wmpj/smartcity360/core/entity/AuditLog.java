package com.wmpj.smartcity360.core.entity;
import jakarta.persistence.*; import java.time.OffsetDateTime;
@Entity @Table(name="audit_logs") public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id",nullable=false,updatable=false) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=true) @JoinColumn(name="actor_id") private User actor;
 @Column(name="action",nullable=false,length=80) private String action; @Column(name="entity_type",nullable=false,length=80) private String entityType;
 @Column(name="entity_id") private Long entityId; @Column(name="details",columnDefinition="jsonb") private String details;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
 protected AuditLog(){}
    public AuditLog(
            User actor,
            String action,
            String entityType,
            Long entityId,
            OffsetDateTime createdAt
    ) {
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.createdAt = createdAt;
    }
 public Long getId(){return id;} public User getActor(){return actor;} public void setActor(User v){actor=v;} public String getAction(){return action;} public void setAction(String v){action=v;} public String getEntityType(){return entityType;} public void setEntityType(String v){entityType=v;} public Long getEntityId(){return entityId;} public void setEntityId(Long v){entityId=v;} public String getDetails(){return details;} public void setDetails(String v){details=v;} public OffsetDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(OffsetDateTime v){createdAt=v;}
}
