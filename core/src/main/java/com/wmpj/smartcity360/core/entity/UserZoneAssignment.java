package com.wmpj.smartcity360.core.entity;
import jakarta.persistence.*; import java.time.OffsetDateTime;
@Entity @Table(name="user_zone_assignments",uniqueConstraints=@UniqueConstraint(name="uq_user_zone",columnNames={"user_id","zone_id"})) public class UserZoneAssignment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id",nullable=false,updatable=false) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="zone_id",nullable=false) private Zone zone;
 @ManyToOne(fetch=FetchType.LAZY,optional=true) @JoinColumn(name="assigned_by") private User assignedBy;
 @Column(name="assigned_at",nullable=false) private OffsetDateTime assignedAt;
 protected UserZoneAssignment(){} public Long getId(){return id;} public User getUser(){return user;} public void setUser(User v){user=v;} public Zone getZone(){return zone;} public void setZone(Zone v){zone=v;} public User getAssignedBy(){return assignedBy;} public void setAssignedBy(User v){assignedBy=v;} public OffsetDateTime getAssignedAt(){return assignedAt;} public void setAssignedAt(OffsetDateTime v){assignedAt=v;}
}
