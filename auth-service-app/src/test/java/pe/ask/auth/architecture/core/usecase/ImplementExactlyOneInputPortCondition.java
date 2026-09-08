package pe.ask.auth.architecture.core.usecase;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.List;

import static pe.ask.auth.architecture.ArchitecturePackages.PORT_IN_PACKAGE;

final class ImplementExactlyOneInputPortCondition
        extends ArchCondition<JavaClass> {

    ImplementExactlyOneInputPortCondition() {
        super("implement exactly one input port");
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        List<JavaClass> inputPorts =
                item.getAllRawInterfaces()
                        .stream()
                        .filter(javaClass ->
                                javaClass.getPackageName()
                                        .equals(PORT_IN_PACKAGE)
                        )
                        .toList();

        if (inputPorts.size() != 1) {
            events.add(
                    SimpleConditionEvent.violated(
                            item,
                            item.getName()
                                    + " debe implementar exactamente un port.in, "
                                    + "pero implementa "
                                    + inputPorts.size()
                                    + ": "
                                    + inputPorts.stream()
                                    .map(JavaClass::getName)
                                    .toList()
                    )
            );
        }
    }
}
