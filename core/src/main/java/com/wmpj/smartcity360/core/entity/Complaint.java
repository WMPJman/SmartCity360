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
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "complaint_number", nullable = false, unique = true, length = 40)
    private String complaintNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resident_id", nullable = false)
    private User resident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ComplaintCategory category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private ComplaintSeverity severity = ComplaintSeverity.LOW;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_to", nullable = true)
    private User assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_handler_level", nullable = false, length = 30)
    private ComplaintHandlerLevel currentHandlerLevel = ComplaintHandlerLevel.LOCAL_STAFF;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    protected Complaint() {
    }

    public Complaint(
            String complaintNumber,
            User resident,
            Zone zone,
            ComplaintCategory category,
            String title,
            String description,
            ComplaintSeverity severity,
            OffsetDateTime createdAt
    ) {
        this.complaintNumber = complaintNumber;
        this.resident = resident;
        this.zone = zone;
        this.category = category;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = ComplaintStatus.SUBMITTED;
        this.currentHandlerLevel = ComplaintHandlerLevel.LOCAL_STAFF;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getComplaintNumber() {
        return complaintNumber;
    }

    public void setComplaintNumber(String complaintNumber) {
        this.complaintNumber = complaintNumber;
    }

    public User getResident() {
        return resident;
    }

    public void setResident(User resident) {
        this.resident = resident;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ComplaintSeverity severity) {
        this.severity = severity;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public ComplaintHandlerLevel getCurrentHandlerLevel() {
        return currentHandlerLevel;
    }

    public void setCurrentHandlerLevel(ComplaintHandlerLevel currentHandlerLevel) {
        this.currentHandlerLevel = currentHandlerLevel;
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

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
