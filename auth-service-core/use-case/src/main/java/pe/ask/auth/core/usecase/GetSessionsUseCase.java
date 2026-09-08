package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.in.GetSessionsInputPort;
import pe.ask.auth.core.port.in.command.GetSessionsCommand;
import pe.ask.auth.core.port.in.result.GetSessionsResult;
import pe.ask.auth.core.port.in.result.SessionSummary;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;


@UseCase
public final class GetSessionsUseCase implements GetSessionsInputPort {

    private final SessionRepositoryOutputPort sessionRepository;

    public GetSessionsUseCase(SessionRepositoryOutputPort sessionRepository) {
        this.sessionRepository = DomainValidation.requireNonNull(sessionRepository, "sessionRepository");
    }

    @Override
    public Mono<GetSessionsResult> getSessions(GetSessionsCommand command) {
        return sessionRepository.findActiveByUserId(command.userId())
                .map(session -> new SessionSummary(
                        session.id(),
                        session.deviceId(),
                        session.ipAddress(),
                        session.userAgent(),
                        session.createdAt(),
                        session.lastActivityAt(),
                        session.expiresAt()
                ))
                .collectList()
                .map(GetSessionsResult::new);
    }
}
