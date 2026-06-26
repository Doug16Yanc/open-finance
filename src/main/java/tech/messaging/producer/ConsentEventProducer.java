package tech.messaging.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import tech.dto.ConsentEventPayload;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class ConsentEventProducer {

    private static final Logger log = Logger.getLogger(ConsentEventProducer.class);

    @Inject
    @Channel("consent-events-out")
    Emitter<String> emitter;

    @Inject
    ObjectMapper objectMapper;

    public void publish(ConsentEventPayload payload) {
        try {
            var json = objectMapper.writeValueAsString(payload);

            var headers = new RecordHeaders();
            headers.add("eventType", payload.eventType().getBytes(StandardCharsets.UTF_8));

            var metadata = OutgoingKafkaRecordMetadata.<String>builder()
                    .withKey(payload.consentId())
                    .withHeaders(headers)
                    .build();

            emitter.send(Message.of(json).addMetadata(metadata));

            log.infof("Evento publicado: %s | consent: %s", payload.eventType(), payload.consentId());

        } catch (JsonProcessingException e) {
            log.errorf(e, "Falha ao serializar evento: %s", payload.eventType());
            throw new RuntimeException("Erro ao publicar evento no Kafka", e);
        }
    }
}