package tech.exception;

public class ConsentNotFoundException extends RuntimeException {
    public ConsentNotFoundException(String consentId) {
        super("Consentimento não encontrado: " + consentId);
    }
}
