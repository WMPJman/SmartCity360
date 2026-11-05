package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "stream_configurations")
public class StreamConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false, unique = true)
    private EnergyMeter meter;

    @Column(name = "enable_encryption", nullable = false)
    private boolean enableEncryption;

    @Column(name = "enable_authentication", nullable = false)
    private boolean enableAuthentication;

    @Column(name = "enable_compression", nullable = false)
    private boolean enableCompression;

    @Column(name = "layer_order", nullable = false, columnDefinition = "jsonb")
    private String layerOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "updated_by", nullable = true)
    private User updatedBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected StreamConfiguration() {
    }

    public Long getId() {
        return id;
    }

    public EnergyMeter getMeter() {
        return meter;
    }

    public void setMeter(EnergyMeter meter) {
        this.meter = meter;
    }

    public boolean isEnableEncryption() {
        return enableEncryption;
    }

    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    public boolean isEnableAuthentication() {
        return enableAuthentication;
    }

    public void setEnableAuthentication(boolean enableAuthentication) {
        this.enableAuthentication = enableAuthentication;
    }

    public boolean isEnableCompression() {
        return enableCompression;
    }

    public void setEnableCompression(boolean enableCompression) {
        this.enableCompression = enableCompression;
    }

    public String getLayerOrder() {
        return layerOrder;
    }

    public void setLayerOrder(String layerOrder) {
        this.layerOrder = layerOrder;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
