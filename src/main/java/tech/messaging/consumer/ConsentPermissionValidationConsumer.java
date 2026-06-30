package tech.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import tech.domain.entity.Consent;
import tech.domain.entity.ConsentPermissionValidation;
import tech.domain.entity.PermissionGroup;
import tech.domain.enums.ConsentStatus;
import tech.domain.enums.ValidationResult;
import tech.dto.ConsentEventPayload;
import tech.messaging.producer.ConsentEventProducer;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class ConsentPermissionValidationConsumer {

    private static final Logger log = Logger.getLogger(ConsentPermissionValidationConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ConsentEventProducer consentEventProducer;

    @Incoming("consent-events-in")
    public CompletionStage<Void> consume(Message<String> message) {
        try {
            var eventType = extractHeader(message, "eventType");

            if (!"CONSENT_CREATED".equals(eventType)) {
                return message.ack();
            }

            var payload = objectMapper.readValue(message.getPayload(), ConsentEventPayload.class);
            log.infof("Validando permissões do consentimento: %s", payload.consentId());

            process(payload);

            return message.ack();

        } catch (Exception e) {
            log.errorf(e, "Erro ao processar validação de permissões: %s", e.getMessage());
            return message.nack(e);
        }
    }

    @Transactional
    void process(ConsentEventPayload payload) {
        var consent = Consent.findByConsentId(payload.consentId());
        if (consent == null) {
            log.warnf("Consentimento não encontrado: %s — ignorando", payload.consentId());
            return;
        }

        var existing = ConsentPermissionValidation.findByConsentId(payload.consentId());
        if (existing != null) {
            log.infof("Validação já existente para %s — ignorando duplicata", payload.consentId());
            return;
        }

        var requested = consent.permissions;
        var allKnown  = resolveAllKnownPermissions();

        var invalid  = findInvalidPermissions(requested, allKnown);
        var missing  = findMissingDependencies(requested, allKnown);
        var resolved = buildResolvedList(requested, missing);

        var validation = new ConsentPermissionValidation();
        validation.consentId            = payload.consentId();
        validation.requestedPermissions = requested;
        validation.invalidPermissions   = invalid;
        validation.missingDependencies  = missing;
        validation.resolvedPermissions  = resolved;

        if (!invalid.isEmpty()) {
            var reason = "Permissões inválidas: " + String.join(", ", invalid);
            validation.result          = ValidationResult.REJECTED;
            validation.rejectionReason = reason;
            validation.persist();

            consent.reject(reason);
            consentEventProducer.publish(new ConsentEventPayload(
                    "CONSENT_REJECTED",
                    payload.consentId(),
                    payload.clientId(),
                    payload.cpf(),
                    ConsentStatus.REJECTED,
                    Collections.emptyList(),
                    OffsetDateTime.now()
            ));
            log.warnf("Consentimento %s rejeitado — %s", payload.consentId(), reason);
            return;
        }

        if (!missing.isEmpty()) {
            validation.result          = ValidationResult.AUTO_CORRECTED;
            validation.rejectionReason = null;
            validation.persist();

            consent.permissions = resolved;
            log.infof("Consentimento %s auto-corrigido. Adicionadas: %s",
                    payload.consentId(), missing);
            return;
        }

        validation.result          = ValidationResult.VALID;
        validation.rejectionReason = null;
        validation.persist();
        log.infof("Permissões do consentimento %s validadas com sucesso", payload.consentId());
    }

    private List<String> resolveAllKnownPermissions() {
        return PermissionGroup.findAllActive()
                .stream()
                .flatMap(g -> g.permissions.stream())
                .distinct()
                .toList();
    }

    private List<String> findInvalidPermissions(List<String> requested, List<String> allKnown) {
        return requested.stream()
                .filter(p -> !allKnown.contains(p))
                .toList();
    }

    private List<String> findMissingDependencies(List<String> requested,
                                                 List<String> allKnown) {
        var missing = new ArrayList<String>();

        for (var group : PermissionGroup.findAllActive()) {
            boolean groupIsRequested = group.permissions.stream()
                    .anyMatch(requested::contains);

            if (groupIsRequested && group.hasRequiredPermissions()) {
                group.requiredPermissions.stream()
                        .filter(dep -> !requested.contains(dep))
                        .filter(dep -> !missing.contains(dep))
                        .forEach(missing::add);
            }
        }

        return missing;
    }

    private List<String> buildResolvedList(List<String> requested,
                                           List<String> missing) {
        var resolved = new ArrayList<>(requested);
        missing.stream()
                .filter(dep -> !resolved.contains(dep))
                .forEach(resolved::add);
        return resolved;
    }

    private String extractHeader(Message<String> message, String headerName) {
        return message.getMetadata()
                .get(io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata.class)
                .map(meta -> {
                    var header = meta.getHeaders().lastHeader(headerName);
                    return header != null
                            ? new String(header.value(), StandardCharsets.UTF_8)
                            : null;
                })
                .orElse(null);
    }
}
