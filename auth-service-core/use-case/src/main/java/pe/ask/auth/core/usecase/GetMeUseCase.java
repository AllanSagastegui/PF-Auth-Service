package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.exception.UserNotFoundException;
import pe.ask.auth.core.port.in.GetMeInputPort;
import pe.ask.auth.core.port.in.command.GetMeCommand;
import pe.ask.auth.core.port.in.result.GetMeResult;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@UseCase
public final class GetMeUseCase implements GetMeInputPort {

    private final UserRepositoryOutputPort userRepository;

    public GetMeUseCase(UserRepositoryOutputPort userRepository) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
    }

    @Override
    public Mono<GetMeResult> getMe(GetMeCommand command) {
        return userRepository.findById(command.userId())
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .map(user -> new GetMeResult(
                        user.id(),
                        user.rawEmail(),
                        user.status().name(),
                        user.roles().stream().map(Role::name).collect(Collectors.toSet()),
                        user.mfaEnabled(),
                        user.createdAt()
                ));
    }
}
