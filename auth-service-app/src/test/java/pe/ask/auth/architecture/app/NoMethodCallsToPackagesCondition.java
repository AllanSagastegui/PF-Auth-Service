package pe.ask.auth.architecture.app;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import pe.ask.auth.architecture.ArchitecturePackages;

import java.util.Arrays;
import java.util.List;

final class NoMethodCallsToPackagesCondition
        extends ArchCondition<JavaClass> {

    private final List<String> forbiddenPackagePrefixes;

    private NoMethodCallsToPackagesCondition(
            String... forbiddenPackages
    ) {
        super(
                "not call methods declared inside packages "
                        + Arrays.toString(forbiddenPackages)
        );

        this.forbiddenPackagePrefixes = Arrays.stream(forbiddenPackages)
                .map(NoMethodCallsToPackagesCondition::normalize)
                .toList();
    }

    static NoMethodCallsToPackagesCondition notCallMethodsIn(
            ) {
        return new NoMethodCallsToPackagesCondition(new String[]{ArchitecturePackages.PORT_IN, ArchitecturePackages.PORT_OUT, ArchitecturePackages.USE_CASE});
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        for (JavaMethodCall call : item.getMethodCallsFromSelf()) {
            String targetPackage =
                    call.getTargetOwner().getPackageName();

            boolean forbidden = forbiddenPackagePrefixes.stream()
                    .anyMatch(prefix ->
                            targetPackage.equals(prefix)
                                    || targetPackage.startsWith(prefix + ".")
                    );

            if (forbidden) {
                events.add(
                        SimpleConditionEvent.violated(
                                item,
                                call.getDescription()
                                        + " ejecuta lógica que pertenece "
                                        + "a un puerto o caso de uso"
                        )
                );
            }
        }
    }

    private static String normalize(String packagePattern) {
        return packagePattern.endsWith("..")
                ? packagePattern.substring(
                0,
                packagePattern.length() - 2
        )
                : packagePattern;
    }
}
