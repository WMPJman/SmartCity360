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
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "complaint_history")
public class ComplaintHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "actor_id", nullable = true)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 40)
    private ComplaintHistoryAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_handler_level", length = 30)
    private ComplaintHandlerLevel fromHandlerLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_handler_level", length = 30)
    private ComplaintHandlerLevel toHandlerLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private ComplaintStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 20)
    private ComplaintStatus toStatus;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ComplaintHistory() {
    }

    public ComplaintHistory(
            Complaint complaint,
            User actor,
            ComplaintHistoryAction action,
            ComplaintHandlerLevel fromHandlerLevel,
            ComplaintHandlerLevel toHandlerLevel,
            ComplaintStatus fromStatus,
            ComplaintStatus toStatus,
            String notes,
            OffsetDateTime createdAt
    ) {
        this.complaint = complaint;
        this.actor = actor;
        this.action = action;
        this.fromHandlerLevel = fromHandlerLevel;
        this.toHandlerLevel = toHandlerLevel;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public ComplaintHistoryAction getAction() {
        return action;
    }

    public void setAction(ComplaintHistoryAction action) {
        this.action = action;
    }

    public ComplaintHandlerLevel getFromHandlerLevel() {
        return fromHandlerLevel;
    }

    public void setFromHandlerLevel(ComplaintHandlerLevel fromHandlerLevel) {
        this.fromHandlerLevel = fromHandlerLevel;
    }

    public ComplaintHandlerLevel getToHandlerLevel() {
        return toHandlerLevel;
    }

    public void setToHandlerLevel(ComplaintHandlerLevel toHandlerLevel) {
        this.toHandlerLevel = toHandlerLevel;
    }

    public ComplaintStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ComplaintStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ComplaintStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ComplaintStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
