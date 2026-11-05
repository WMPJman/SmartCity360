package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.energy.BasicEnergyStream;
import com.wmpj.smartcity360.core.energy.EnergyProcessingResult;
import com.wmpj.smartcity360.core.energy.EnergyStreamOperation;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.EnergyProcessingService;
import com.wmpj.smartcity360.web.dto.EnergyProcessRequest;
import com.wmpj.smartcity360.web.dto.EnergyProcessResponse;
import com.wmpj.smartcity360.web.security.BusinessApiSecurity;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Base64;
import java.util.Locale;

@Path("/energy")
@Produces(MediaType.APPLICATION_JSON)
public class EnergyResource {

    private static final int MAX_BASE64_CHARACTERS = 90_000;
    private static final int MAX_TRANSPORT_BYTES =
            BasicEnergyStream.MAX_ENCODED_PAYLOAD_BYTES + 128;

    @EJB
    private EnergyProcessingService energyProcessingService;

    @Inject
    private BusinessApiSecurity businessApiSecurity;

    public EnergyResource() {
    }

    EnergyResource(
            EnergyProcessingService energyProcessingService,
            BusinessApiSecurity businessApiSecurity
    ) {
        this.energyProcessingService = energyProcessingService;
        this.businessApiSecurity = businessApiSecurity;
    }

    @POST
    @Path("/process")
    @Consumes(MediaType.APPLICATION_JSON)
    public EnergyProcessResponse process(EnergyProcessRequest request) {
        businessApiSecurity.requireAnyRole(SecurityRoles.ADMIN);
        validate(request);

        byte[] payload;
        EnergyStreamOperation operation;

        try {
            payload = Base64.getDecoder().decode(request.getPayloadBase64());
            operation = EnergyStreamOperation.valueOf(
                    request.getOperation().trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Energy operation or payload is invalid");
        }

        int maximumPayloadBytes = BasicEnergyStream.MAX_ENCODED_PAYLOAD_BYTES;

        if (operation == EnergyStreamOperation.DECODE) {
            maximumPayloadBytes = MAX_TRANSPORT_BYTES;
        }

        if (payload.length > maximumPayloadBytes) {
            throw new InvalidRequestException("Energy payload is too large");
        }

        EnergyProcessingResult result = energyProcessingService.process(
                request.getMeterCode().trim(),
                payload,
                operation
        );
        byte[] processedPayload = result.payload();

        return new EnergyProcessResponse(
                Base64.getEncoder().encodeToString(processedPayload),
                processedPayload.length,
                result.effectiveOrder()
        );
    }

    private static void validate(EnergyProcessRequest request) {
        if (request == null
                || request.getMeterCode() == null
                || request.getOperation() == null
                || request.getPayloadBase64() == null) {
            throw new InvalidRequestException("Energy processing request is incomplete");
        }

        String meterCode = request.getMeterCode().trim();

        if (!meterCode.matches("[A-Za-z0-9_-]{1,60}")) {
            throw new InvalidRequestException("Meter code is invalid");
        }

        if (request.getPayloadBase64().length() > MAX_BASE64_CHARACTERS) {
            throw new InvalidRequestException("Energy payload is too large");
        }
    }
}
