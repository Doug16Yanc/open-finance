package tech.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger log = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException ex) {
        log.errorf(ex, "Erro não tratado: %s", ex.getMessage());

        if (ex instanceof ConsentNotFoundException) {
            return errorResponse(404, "CONSENT_NOT_FOUND", ex.getMessage());
        }
        if (ex instanceof NotFoundException) {
            return errorResponse(404, "ENDPOINT_NOT_FOUND", "Rota não encontrada");
        }
        if (ex instanceof IllegalStateException) {
            return errorResponse(422, "INVALID_STATE_TRANSITION", ex.getMessage());
        }
        return errorResponse(500, "INTERNAL_ERROR", "Erro interno");
    }

    private Response errorResponse(int status, String code, String message) {
        var body = Map.of(
                "code", code,
                "message", message,
                "timestamp", OffsetDateTime.now().toString()
        );
        return Response.status(status).entity(body).build();
    }
}