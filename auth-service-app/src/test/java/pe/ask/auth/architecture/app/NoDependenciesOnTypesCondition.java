package pe.ask.auth.architecture.app;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

final class NoDependenciesOnTypesCondition
        extends ArchCondition<JavaClass> {

    private final Set<String> forbiddenTypeNames;

    private NoDependenciesOnTypesCondition(
            String... forbiddenTypeNames
    ) {
        super(
                "not depend on forbidden types "
                        + Arrays.toString(forbiddenTypeNames)
        );

        this.forbiddenTypeNames = Arrays.stream(forbiddenTypeNames)
                .collect(Collectors.toUnmodifiableSet());
    }

    static NoDependenciesOnTypesCondition notDependOn(
            String... forbiddenTypeNames
    ) {
        return new NoDependenciesOnTypesCondition(forbiddenTypeNames);
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        for (Dependency dependency :
                item.getDirectDependenciesFromSelf()) {

            String targetType =
                    dependency.getTargetClass().getName();

            if (forbiddenTypeNames.contains(targetType)) {
                events.add(
                        SimpleConditionEvent.violated(
                                item,
                                dependency.getDescription()
                                        + " depende de una API bloqueante "
                                        + "o imperativa prohibida"
                        )
                );
            }
        }
    }
}
