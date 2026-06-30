package tech.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tech.domain.enums.ConsentStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "consents")
public class Consent extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "consent_id", nullable = false, unique = true)
    @NotBlank
    public String consentId;

    @Column(name = "client_id", nullable = false)
    @NotBlank
    public String clientId;

    @Column(name = "cpf", nullable = false)
    @NotBlank
    public String cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    public ConsentStatus status;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "permissions", nullable = false, columnDefinition = "text[]")
    @NotNull
    public List<String> permissions;

    @Column(name = "expiration_date")
    public OffsetDateTime expirationDate;

    @Column(name = "transaction_from")
    public OffsetDateTime transactionFrom;

    @Column(name = "transaction_to")
    public OffsetDateTime transactionTo;

    @Column(name = "rejection_reason")
    public String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    public Long version;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
        status = ConsentStatus.AWAITING_AUTHORISATION;
        consentId = "urn:ofb:consent:" + UUID.randomUUID();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public static Consent findByConsentId(String consentId) {
        return find("consentId", consentId).firstResult();
    }

    public static List<Consent> findByClientAndCpf(String clientId, String cpf) {
        return list("clientId = ?1 and cpf = ?2", clientId, cpf);
    }

    public static List<Consent> findByStatus(ConsentStatus status) {
        return list("status", status);
    }

    public void authorise() {
        if (status != ConsentStatus.AWAITING_AUTHORISATION) {
            throw new IllegalStateException(
                    "Consentimento não pode ser autorizado no status: " + status
            );
        }
        status = ConsentStatus.AUTHORISED;
    }

    public void reject(String reason) {
        if (status != ConsentStatus.AWAITING_AUTHORISATION) {
            throw new IllegalStateException(
                    "Consentimento não pode ser rejeitado no status: " + status
            );
        }
        status = ConsentStatus.REJECTED;
        rejectionReason = reason;
    }

    public void revoke(String reason) {
        if (status != ConsentStatus.AUTHORISED) {
            throw new IllegalStateException(
                    "Apenas consentimentos AUTHORISED podem ser revogados"
            );
        }
        status = ConsentStatus.REVOKED;
        rejectionReason = reason;
    }

    public boolean isExpired() {
        return expirationDate != null && OffsetDateTime.now().isAfter(expirationDate);
    }
}