package com.wmpj.smartcity360.core.energy;

import javax.crypto.SecretKey;
import java.util.Objects;

public final class EnergyStreamPipelineFactory {

    public EnergyStreamComponent create(
            String meterCode,
            EnergyStreamConfiguration configuration,
            EnergyStreamKeys keys
    ) {
        Objects.requireNonNull(meterCode, "meterCode");
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(keys, "keys");

        EnergyStreamComponent pipeline = new BasicEnergyStream();

        if (configuration.compressionEnabled()) {
            pipeline = new CompressionEnergyStreamDecorator(pipeline);
        }

        if (configuration.encryptionEnabled()) {
            SecretKey encryptionKey = keys.encryptionKey();

            if (encryptionKey == null) {
                throw new EnergyKeyUnavailableException("Energy encryption key is unavailable");
            }

            pipeline = new EncryptionEnergyStreamDecorator(
                    pipeline,
                    encryptionKey,
                    meterCode
            );
        }

        if (configuration.authenticationEnabled()) {
            SecretKey authenticationKey = keys.authenticationKey();

            if (authenticationKey == null) {
                throw new EnergyKeyUnavailableException(
                        "Energy authentication key is unavailable"
                );
            }

            pipeline = new AuthenticationEnergyStreamDecorator(
                    pipeline,
                    authenticationKey,
                    meterCode
            );
        }

        return pipeline;
    }
}
