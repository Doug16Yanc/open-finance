package tech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateConsentRequest(
        @NotBlank String cpf,
        @NotEmpty List<@NotBlank String> permissions,
        OffsetDateTime expirationDate,
        OffsetDateTime transactionFrom,
        OffsetDateTime transactionTo
) {}

