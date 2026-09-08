package pe.ask.auth.output.kafka.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationKafkaAdapter tests")
class NotificationKafkaAdapterTest {

    @Mock
    private KafkaSender<String, String> kafkaSender;

    @Test
    @DisplayName("Should return empty mono when kafkaSender is null")
    void shouldReturnEmptyWhenSenderNull() {
        NotificationKafkaAdapter adapter = new NotificationKafkaAdapter();

        StepVerifier.create(adapter.sendVerificationEmail("user@example.com", "token-123"))
                .verifyComplete();

        StepVerifier.create(adapter.sendPasswordResetEmail("user@example.com", "token-456"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should send verification email successfully when kafkaSender is provided")
    void shouldSendVerificationEmail() {
        when(kafkaSender.send(any())).thenReturn(Flux.empty());
        NotificationKafkaAdapter adapter = new NotificationKafkaAdapter(kafkaSender, "custom-topic");

        StepVerifier.create(adapter.sendVerificationEmail("user@example.com", "verify-token"))
                .verifyComplete();

        verify(kafkaSender).send(any());
    }

    @Test
    @DisplayName("Should send password reset email successfully")
    void shouldSendPasswordResetEmail() {
        when(kafkaSender.send(any())).thenReturn(Flux.empty());
        NotificationKafkaAdapter adapter = new NotificationKafkaAdapter(kafkaSender);

        StepVerifier.create(adapter.sendPasswordResetEmail("user@example.com", "reset-token"))
                .verifyComplete();

        verify(kafkaSender).send(any());
    }

    @Test
    @DisplayName("Should validate required arguments")
    void shouldValidateArguments() {
        NotificationKafkaAdapter adapter = new NotificationKafkaAdapter(kafkaSender);

        assertThrows(RequiredArgumentException.class, () -> adapter.sendVerificationEmail(null, "token"));
        assertThrows(RequiredArgumentException.class, () -> adapter.sendVerificationEmail("email", null));
        assertThrows(RequiredArgumentException.class, () -> adapter.sendPasswordResetEmail(null, "token"));
        assertThrows(RequiredArgumentException.class, () -> adapter.sendPasswordResetEmail("email", null));
    }
}
