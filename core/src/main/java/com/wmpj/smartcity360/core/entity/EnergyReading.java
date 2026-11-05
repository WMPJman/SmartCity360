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
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "energy_readings",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_energy_interval",
                columnNames = {"meter_id", "interval_start"}
        )
)
public class EnergyReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private EnergyMeter meter;

    @Column(name = "interval_start", nullable = false)
    private OffsetDateTime intervalStart;

    @Column(name = "interval_end", nullable = false)
    private OffsetDateTime intervalEnd;

    @Column(name = "consumption_kwh", nullable = false, precision = 14, scale = 4)
    private BigDecimal consumptionKwh;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private EnergyReadingSource source;

    protected EnergyReading() {
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

    public OffsetDateTime getIntervalStart() {
        return intervalStart;
    }

    public void setIntervalStart(OffsetDateTime intervalStart) {
        this.intervalStart = intervalStart;
    }

    public OffsetDateTime getIntervalEnd() {
        return intervalEnd;
    }

    public void setIntervalEnd(OffsetDateTime intervalEnd) {
        this.intervalEnd = intervalEnd;
    }

    public BigDecimal getConsumptionKwh() {
        return consumptionKwh;
    }

    public void setConsumptionKwh(BigDecimal consumptionKwh) {
        this.consumptionKwh = consumptionKwh;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public EnergyReadingSource getSource() {
        return source;
    }

    public void setSource(EnergyReadingSource source) {
        this.source = source;
    }
}
