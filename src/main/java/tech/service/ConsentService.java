package tech.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.domain.entity.Consent;
import tech.domain.enums.ConsentStatus;
import tech.dto.*;
import tech.exception.ConsentNotFoundException;
import tech.messaging.producer.ConsentEventProducer;

import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class ConsentService {

    @Inject
    ConsentEventProducer eventProducer;

    @ConfigProperty(name = "ofb.consent.default-expiration-days", defaultValue = "90")
    int defaultExpirationDays;

    @Transactional
    public ConsentResponse create(String clientId, CreateConsentRequest request) {
        var consent = new Consent();
        consent.clientId = clientId;
        consent.cpf = request.cpf();
        consent.permissions = request.permissions();
        consent.expirationDate = request.expirationDate() != null
                ? request.expirationDate()
                : OffsetDateTime.now().plusDays(defaultExpirationDays);
        consent.transactionFrom = request.transactionFrom();
        consent.transactionTo = request.transactionTo();

        consent.persist();

        eventProducer.publish(new ConsentEventPayload(
                "ConsentCreated",
                consent.consentId,
                consent.clientId,
                consent.cpf,
                consent.status,
                consent.permissions,
                consent.createdAt
        ));

        return ConsentResponse.from(consent);
    }

    public ConsentResponse findByConsentId(String consentId) {
        var consent = Consent.findByConsentId(consentId);
        if (consent == null) throw new ConsentNotFoundException(consentId);
        return ConsentResponse.from(consent);
    }

    public List<ConsentResponse> listByClientAndCpf(String clientId, String cpf) {
        return Consent.findByClientAndCpf(clientId, cpf)
                .stream()
                .map(ConsentResponse::from)
                .toList();
    }

    @Transactional
    public ConsentResponse authorise(String consentId) {
        var consent = findConsent(consentId);
        consent.authorise();

        eventProducer.publish(new ConsentEventPayload(
                "ConsentAuthorised",
                consent.consentId, consent.clientId, consent.cpf,
                ConsentStatus.AUTHORISED, consent.permissions,
                OffsetDateTime.now()
        ));

        return ConsentResponse.from(consent);
    }

    @Transactional
    public ConsentResponse reject(String consentId, RejectConsentRequest request) {
        var consent = findConsent(consentId);
        consent.reject(request.reason());

        eventProducer.publish(new ConsentEventPayload(
                "ConsentRejected",
                consent.consentId, consent.clientId, consent.cpf,
                ConsentStatus.REJECTED, consent.permissions,
                OffsetDateTime.now()
        ));

        return ConsentResponse.from(consent);
    }

    @Transactional
    public ConsentResponse revoke(String consentId, RevokeConsentRequest request) {
        var consent = findConsent(consentId);
        consent.revoke(request.reason());

        eventProducer.publish(new ConsentEventPayload(
                "ConsentRevoked",
                consent.consentId, consent.clientId, consent.cpf,
                ConsentStatus.REVOKED, consent.permissions,
                OffsetDateTime.now()
        ));

        return ConsentResponse.from(consent);
    }

    private Consent findConsent(String consentId) {
        var consent = Consent.findByConsentId(consentId);
        if (consent == null) throw new ConsentNotFoundException(consentId);
        return consent;
    }
}
