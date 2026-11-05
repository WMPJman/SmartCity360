package com.wmpj.smartcity360.core.energy;

public interface EnergyStreamComponent {

    byte[] encode(byte[] payload);

    byte[] decode(byte[] payload);
}
