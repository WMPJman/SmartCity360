package com.wmpj.smartcity360.core.energy;

import javax.crypto.SecretKey;

public record EnergyStreamKeys(SecretKey authenticationKey, SecretKey encryptionKey) {
}
