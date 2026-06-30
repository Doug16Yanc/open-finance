package tech.dto;

import tech.domain.entity.Consent;
import tech.domain.enums.ConsentStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ConsentResponse(
        UUID id,
        String consentId,
        String clientId,
        String cpf,
        ConsentStatus status,
        List<String> permissions,
        OffsetDateTime expirationDate,
        OffsetDateTime transactionFrom,
        OffsetDateTime transactionTo,
        String rejectionReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Long version
) {
    public static ConsentResponse from(Consent c) {
        return new ConsentResponse(
                c.id, c.consentId, c.clientId, c.cpf,
                c.status, c.permissions,
                c.expirationDate, c.transactionFrom, c.transactionTo,
                c.rejectionReason, c.createdAt, c.updatedAt, c.version
        );
    }
}