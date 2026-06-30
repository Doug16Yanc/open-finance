package tech.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tech.domain.enums.ValidationResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "consent_permission_validations")
public class ConsentPermissionValidation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "consent_id", nullable = false, unique = true)
    @NotBlank
    public String consentId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "requested_permissions", nullable = false, columnDefinition = "text[]")
    @NotNull
    public List<String> requestedPermissions;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "missing_dependencies", columnDefinition = "text[]")
    public List<String> missingDependencies;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "invalid_permissions", columnDefinition = "text[]")
    public List<String> invalidPermissions;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "resolved_permissions", columnDefinition = "text[]")
    public List<String> resolvedPermissions;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    @NotNull
    public ValidationResult result;

    @Column(name = "rejection_reason")
    public String rejectionReason;

    @Column(name = "validated_at", nullable = false)
    public OffsetDateTime validatedAt;

    @PrePersist
    void onCreate() {
        validatedAt = OffsetDateTime.now();
    }

    public static ConsentPermissionValidation findByConsentId(String consentId) {
        return find("consentId", consentId).firstResult();
    }

    public static List<ConsentPermissionValidation> findByResult(ValidationResult result) {
        return list("result", result);
    }

    public boolean isValid() {
        return result == ValidationResult.VALID || result == ValidationResult.AUTO_CORRECTED;
    }

    public boolean wasAutoCorrected() {
        return result == ValidationResult.AUTO_CORRECTED;
    }

    public boolean hasMissingDependencies() {
        return missingDependencies != null && !missingDependencies.isEmpty();
    }

    public boolean hasInvalidPermissions() {
        return invalidPermissions != null && !invalidPermissions.isEmpty();
    }
}