package pe.ask.auth.architecture.app;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static pe.ask.auth.architecture.ArchitecturePackages.APPLICATION;

public final class ReactiveCallArchitectureRules {

    private ReactiveCallArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Prohíbe en código productivo:
     * - block/blockFirst/blockLast.
     * - subscribe/subscribeWith.
     * - toFuture/toIterable/toStream.
     * - boundedElastic.
     * - Future.get/join.
     * - Thread.sleep/join/start.
     * - ExecutorService.
     * - Locks, latches, barriers y semáforos.
     * - BlockingQueue.
     * - parallelStream.
     * - HTTP síncrono.
     * - Process.waitFor.
     */
    @ArchTest
    static final ArchRule APPLICATION_MUST_NOT_CALL_BLOCKING_APIS =
            classes()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should(new ForbiddenReactiveCallsCondition())
                    .because(
                            "la aplicación debe conservar una única cadena "
                                    + "reactiva administrada por el framework"
                    );
}