package com.wmpj.smartcity360.core.energy;

import java.util.Objects;

public abstract class AbstractEnergyStreamDecorator implements EnergyStreamComponent {

    protected final EnergyStreamComponent wrappedComponent;

    protected AbstractEnergyStreamDecorator(EnergyStreamComponent wrappedComponent) {
        this.wrappedComponent = Objects.requireNonNull(
                wrappedComponent,
                "wrappedComponent"
        );
    }
}
