package pe.ask.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.OutboxRepositoryOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.output.database.adapter.OneTimeTokenDatabaseAdapter;
import pe.ask.auth.output.database.adapter.OutboxDatabaseAdapter;
import pe.ask.auth.output.database.adapter.RefreshTokenDatabaseAdapter;
import pe.ask.auth.output.database.adapter.SessionDatabaseAdapter;
import pe.ask.auth.output.database.adapter.UserDatabaseAdapter;
import pe.ask.auth.output.database.repository.OneTimeTokenR2dbcRepository;
import pe.ask.auth.output.database.repository.OutboxMessageR2dbcRepository;
import pe.ask.auth.output.database.repository.RefreshTokenR2dbcRepository;
import pe.ask.auth.output.database.repository.SessionR2dbcRepository;
import pe.ask.auth.output.database.repository.UserR2dbcRepository;

@Configuration(proxyBeanMethods = false)
@EnableR2dbcRepositories(basePackages = "pe.ask.auth.output.database.repository")
public final class DatabaseOutputConfiguration {

    @Bean
    public UserRepositoryOutputPort userRepositoryOutputPort(UserR2dbcRepository repository) {
        return new UserDatabaseAdapter(repository);
    }

    @Bean
    public SessionRepositoryOutputPort sessionRepositoryOutputPort(SessionR2dbcRepository repository) {
        return new SessionDatabaseAdapter(repository);
    }

    @Bean
    public RefreshTokenRepositoryOutputPort refreshTokenRepositoryOutputPort(RefreshTokenR2dbcRepository repository) {
        return new RefreshTokenDatabaseAdapter(repository);
    }

    @Bean
    public OneTimeTokenRepositoryOutputPort oneTimeTokenRepositoryOutputPort(OneTimeTokenR2dbcRepository repository) {
        return new OneTimeTokenDatabaseAdapter(repository);
    }

    @Bean
    public OutboxRepositoryOutputPort outboxRepositoryOutputPort(OutboxMessageR2dbcRepository repository) {
        return new OutboxDatabaseAdapter(repository);
    }
}
