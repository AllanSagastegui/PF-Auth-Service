package pe.ask.auth.architecture.core;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

final class CoreDeterminismCondition
        extends ArchCondition<JavaClass> {

    private static final Set<String> TIME_TYPES =
            Set.of(
                    Instant.class.getName(),
                    LocalDate.class.getName(),
                    LocalDateTime.class.getName(),
                    LocalTime.class.getName(),
                    OffsetDateTime.class.getName(),
                    ZonedDateTime.class.getName()
            );

    CoreDeterminismCondition() {
        super("not access hidden sources of non-determinism");
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

            String method =
                    call.getName();

            /*
             * Instant.now(), LocalDate.now(), etc.
             *
             * La variante now(Clock) sí está permitida porque
             * Clock es una dependencia explícita.
             */
            if (TIME_TYPES.contains(owner)
                    && method.equals("now")
                    && call.getTarget()
                    .getRawParameterTypes()
                    .isEmpty()) {

                violation(
                        item,
                        call,
                        events,
                        "debe utilizarse Clock explícito"
                );

                continue;
            }

            if (owner.equals(UUID.class.getName())
                    && method.equals("randomUUID")) {

                violation(
                        item,
                        call,
                        events,
                        "la generación de IDs debe ser una "
                                + "dependencia explícita"
                );

                continue;
            }

            if (owner.equals(Math.class.getName())
                    && method.equals("random")) {

                violation(
                        item,
                        call,
                        events,
                        "el random no debe ser una dependencia oculta"
                );

                continue;
            }

            if (owner.equals(System.class.getName())
                    && Set.of(
                    "currentTimeMillis",
                    "nanoTime",
                    "getenv",
                    "getProperty"
            ).contains(method)) {

                violation(
                        item,
                        call,
                        events,
                        "System no debe actuar como dependencia "
                                + "oculta del core"
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
