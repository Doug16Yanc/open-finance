package tech.dto;

import tech.domain.enums.ConsentStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record ConsentEventPayload(
        String eventType,
        String consentId,
        String clientId,
        String cpf,
        ConsentStatus status,
        List<String> permissions,
        OffsetDateTime occurredAt
) {}

