package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.JwksKey;
import pe.ask.auth.core.port.in.command.GetJwksCommand;
import pe.ask.auth.core.port.out.JwksOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetJwksUseCaseTest {

    @Mock
    private JwksOutputPort jwksPort;

    private GetJwksUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetJwksUseCase(jwksPort);
    }

    @Test
    @DisplayName("Should return list of public JWKS keys")
    void shouldReturnPublicJwksKeys() {
        JwksKey key = new JwksKey("EC", "P-256", "auth-key-1", "sig", "ES256", "xCoord", "yCoord");
        when(jwksPort.getPublicKeys()).thenReturn(Mono.just(List.of(key)));

        StepVerifier.create(useCase.getJwks(new GetJwksCommand()))
                .expectNextMatches(res ->
                        res.keys().size() == 1 &&
                        "auth-key-1".equals(res.keys().get(0).kid()) &&
                        "ES256".equals(res.keys().get(0).alg())
                )
                .verifyComplete();
    }
}
