package tech.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import tech.domain.entity.ConsentPermissionValidation;
import tech.domain.enums.ValidationResult;

import java.util.List;

@ApplicationScoped
public class ConsentPermissionValidationService {

    public ConsentPermissionValidation findByConsentId(String consentId) {
        var validation = ConsentPermissionValidation.findByConsentId(consentId);

        if (validation == null) {
            throw new NotFoundException("Validação não encontrada para o consentimento: " + consentId);
        }

        return validation;
    }

    public List<ConsentPermissionValidation> findRejected() {
        return ConsentPermissionValidation.findByResult(ValidationResult.REJECTED);
    }

    public List<ConsentPermissionValidation> findAutoCorrected() {
        return ConsentPermissionValidation.findByResult(ValidationResult.AUTO_CORRECTED);
    }

    public boolean isApproved(String consentId) {
        try {
            var validation = findByConsentId(consentId);
            return validation.isValid();
        } catch (NotFoundException e) {
            return false;
        }
    }
}
