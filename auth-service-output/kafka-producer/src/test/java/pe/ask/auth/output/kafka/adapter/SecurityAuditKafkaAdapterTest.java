package pe.ask.auth.output.kafka.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityAuditKafkaAdapter tests")
class SecurityAuditKafkaAdapterTest {

    @Mock
    private KafkaSender<String, String> kafkaSender;

    @Test
    @DisplayName("Should return empty mono when kafkaSender is null")
    void shouldReturnEmptyWhenSenderNull() {
        SecurityAuditKafkaAdapter adapter = new SecurityAuditKafkaAdapter();

        StepVerifier.create(adapter.recordSecurityEvent("LOGIN_SUCCESS", UUID.randomUUID(), "Login ok", Instant.now()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should send audit message when userId is present")
    void shouldSendAuditWithUser() {
        when(kafkaSender.send(any())).thenReturn(Flux.empty());
        SecurityAuditKafkaAdapter adapter = new SecurityAuditKafkaAdapter(kafkaSender, "audit-topic");

        StepVerifier.create(adapter.recordSecurityEvent("LOGIN_SUCCESS", UUID.randomUUID(), "detail", Instant.now()))
                .verifyComplete();

        verify(kafkaSender).send(any());
    }

    @Test
    @DisplayName("Should send audit message when userId is null")
    void shouldSendAuditWithoutUser() {
        when(kafkaSender.send(any())).thenReturn(Flux.empty());
        SecurityAuditKafkaAdapter adapter = new SecurityAuditKafkaAdapter(kafkaSender);

        StepVerifier.create(adapter.recordSecurityEvent("LOGIN_FAILED", null, "bad pass", null))
                .verifyComplete();

        verify(kafkaSender).send(any());
    }
}
