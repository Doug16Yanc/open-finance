package tech.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectConsentRequest(
        @NotBlank String reason
) {}