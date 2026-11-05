package com.wmpj.smartcity360.ejb.service;

import com.wmpj.smartcity360.core.energy.EnergyProcessingResult;
import com.wmpj.smartcity360.core.energy.EnergyStreamComponent;
import com.wmpj.smartcity360.core.energy.EnergyStreamConfiguration;
import com.wmpj.smartcity360.core.energy.EnergyStreamKeys;
import com.wmpj.smartcity360.core.energy.EnergyStreamOperation;
import com.wmpj.smartcity360.core.energy.EnergyStreamPipelineFactory;
import com.wmpj.smartcity360.core.entity.EnergyMeter;
import com.wmpj.smartcity360.core.entity.EnergyMeterStatus;
import com.wmpj.smartcity360.core.entity.StreamConfiguration;
import com.wmpj.smartcity360.core.exception.ResourceNotFoundException;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.EnergyProcessingService;
import com.wmpj.smartcity360.ejb.security.EnergyStreamKeyProvider;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Objects;

@Stateless
@Local(EnergyProcessingService.class)
@RolesAllowed(SecurityRoles.ADMIN)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class EnergyProcessingServiceBean implements EnergyProcessingService {

    @PersistenceContext(unitName = "SmartCity360PU")
    private EntityManager entityManager;

    @Inject
    private EnergyStreamKeyProvider keyProvider;

    private final EnergyStreamPipelineFactory pipelineFactory = new EnergyStreamPipelineFactory();

    public EnergyProcessingServiceBean() {
    }

    @Override
    public EnergyProcessingResult process(
            String meterCode,
            byte[] payload,
            EnergyStreamOperation operation
    ) {
        Objects.requireNonNull(meterCode, "meterCode");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(operation, "operation");

        EnergyMeter meter = findMeter(meterCode);

        if (meter.getStatus() != EnergyMeterStatus.ACTIVE) {
            throw new ResourceNotFoundException("Energy meter is unavailable");
        }

        StreamConfiguration storedConfiguration = findConfiguration(meter.getId());
        EnergyStreamConfiguration configuration = EnergyStreamConfiguration.fromJson(
                storedConfiguration.isEnableAuthentication(),
                storedConfiguration.isEnableEncryption(),
                storedConfiguration.isEnableCompression(),
                storedConfiguration.getLayerOrder()
        );
        EnergyStreamKeys keys = keyProvider.loadKeys(
                configuration.authenticationEnabled(),
                configuration.encryptionEnabled()
        );
        EnergyStreamComponent pipeline = pipelineFactory.create(
                meter.getMeterCode(),
                configuration,
                keys
        );
        byte[] processedPayload;

        if (operation == EnergyStreamOperation.ENCODE) {
            processedPayload = pipeline.encode(payload);
        } else {
            processedPayload = pipeline.decode(payload);
        }

        return new EnergyProcessingResult(
                processedPayload,
                configuration.effectiveOrder()
        );
    }

    private EnergyMeter findMeter(String meterCode) {
        List<EnergyMeter> meters = entityManager.createQuery(
                        "SELECT m FROM EnergyMeter m JOIN FETCH m.zone "
                                + "WHERE m.meterCode = :meterCode",
                        EnergyMeter.class
                )
                .setParameter("meterCode", meterCode)
                .setMaxResults(1)
                .getResultList();

        if (meters.isEmpty()) {
            throw new ResourceNotFoundException("Energy meter was not found");
        }

        return meters.get(0);
    }

    private StreamConfiguration findConfiguration(Long meterId) {
        List<StreamConfiguration> configurations = entityManager.createQuery(
                        "SELECT c FROM StreamConfiguration c WHERE c.meter.id = :meterId",
                        StreamConfiguration.class
                )
                .setParameter("meterId", meterId)
                .setMaxResults(1)
                .getResultList();

        if (configurations.isEmpty()) {
            throw new ResourceNotFoundException("Energy stream configuration was not found");
        }

        return configurations.get(0);
    }
}
