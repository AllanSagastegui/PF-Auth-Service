package pe.ask.auth.architecture.core;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.Set;

final class CoreReactiveCallsCondition
        extends ArchCondition<JavaClass> {

    private static final Set<String> TERMINAL_OR_BLOCKING_METHODS =
            Set.of(
                    "block",
                    "blockOptional",
                    "blockFirst",
                    "blockLast",
                    "subscribe",
                    "subscribeWith",
                    "toFuture",
                    "toIterable",
                    "toStream"
            );

    private static final Set<String> SCHEDULING_METHODS =
            Set.of(
                    "subscribeOn",
                    "publishOn",
                    "runOn",
                    "parallel"
            );

    private static final Set<String> INFRASTRUCTURE_POLICY_METHODS =
            Set.of(
                    "timeout",
                    "retry",
                    "retryWhen",
                    "repeatWhen",
                    "delayElement",
                    "delaySubscription"
            );

    private static final Set<String> IMPERATIVE_BRIDGE_METHODS =
            Set.of(
                    "fromCallable",
                    "fromRunnable",
                    "fromFuture",
                    "fromCompletionStage"
            );

    CoreReactiveCallsCondition() {
        super(
                "not block, subscribe manually or control "
                        + "reactive infrastructure from core"
        );
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        for (JavaMethodCall call :
                item.getMethodCallsFromSelf()) {

            String owner =
                    call.getTargetOwner().getName();

            if (!owner.startsWith(
                    "reactor.core.publisher."
            )) {
                continue;
            }

            String method =
                    call.getName();

            if (TERMINAL_OR_BLOCKING_METHODS.contains(method)) {
                violation(
                        item,
                        call,
                        events,
                        "rompe o termina manualmente la cadena reactiva"
                );

                continue;
            }

            if (SCHEDULING_METHODS.contains(method)) {
                violation(
                        item,
                        call,
                        events,
                        "el core no debe controlar schedulers o threads"
                );

                continue;
            }

            if (INFRASTRUCTURE_POLICY_METHODS.contains(method)) {
                violation(
                        item,
                        call,
                        events,
                        "timeouts, retries y delays son políticas "
                                + "de infraestructura y no del core"
                );

                continue;
            }

            if (IMPERATIVE_BRIDGE_METHODS.contains(method)) {
                violation(
                        item,
                        call,
                        events,
                        "el core no debe adaptar APIs imperativas "
                                + "a Reactor"
                );
            }
        }
    }

    private void violation(
            JavaClass owner,
            JavaMethodCall call,
            ConditionEvents events,
            String reason
    ) {
        events.add(
                SimpleConditionEvent.violated(
                        owner,
                        call.getDescription()
                                + " está prohibido porque "
                                + reason
                )
        );
    }
}
