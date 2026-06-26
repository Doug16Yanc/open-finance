package tech.dto;

import jakarta.validation.constraints.NotBlank;

public record RevokeConsentRequest(
        @NotBlank String reason
) {}