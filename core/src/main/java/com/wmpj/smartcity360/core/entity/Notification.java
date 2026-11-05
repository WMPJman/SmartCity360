package com.wmpj.smartcity360.core.entity;
import jakarta.persistence.*; import java.time.OffsetDateTime;
@Entity @Table(name="notifications") public class Notification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id",nullable=false,updatable=false) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(name="title",nullable=false,length=150) private String title; @Column(name="message",nullable=false) private String message;
 @Column(name="notification_type",nullable=false,length=40) private String notificationType;
 @ManyToOne(fetch=FetchType.LAZY,optional=true) @JoinColumn(name="related_complaint_id") private Complaint relatedComplaint;
 @Column(name="is_read",nullable=false) private boolean read; @Column(name="created_at",nullable=false) private OffsetDateTime createdAt; @Column(name="read_at") private OffsetDateTime readAt;
 protected Notification(){} public Long getId(){return id;} public User getUser(){return user;} public void setUser(User v){user=v;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getMessage(){return message;} public void setMessage(String v){message=v;} public String getNotificationType(){return notificationType;} public void setNotificationType(String v){notificationType=v;} public Complaint getRelatedComplaint(){return relatedComplaint;} public void setRelatedComplaint(Complaint v){relatedComplaint=v;} public boolean isRead(){return read;} public void setRead(boolean v){read=v;} public OffsetDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(OffsetDateTime v){createdAt=v;} public OffsetDateTime getReadAt(){return readAt;} public void setReadAt(OffsetDateTime v){readAt=v;}
}
