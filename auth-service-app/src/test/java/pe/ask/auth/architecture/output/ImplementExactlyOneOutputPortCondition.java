package pe.ask.auth.architecture.output;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.List;

import static pe.ask.auth.architecture.ArchitecturePackages.PORT_OUT_PACKAGE;

final class ImplementExactlyOneOutputPortCondition
        extends ArchCondition<JavaClass> {

    ImplementExactlyOneOutputPortCondition() {
        super("implement exactly one output port");
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        List<JavaClass> outputPorts =
                item.getAllRawInterfaces()
                        .stream()
                        .filter(javaClass ->
                                javaClass.getPackageName()
                                        .equals(PORT_OUT_PACKAGE)
                        )
                        .toList();

        if (outputPorts.size() != 1) {
            events.add(
                    SimpleConditionEvent.violated(
                            item,
                            item.getName()
                                    + " debe implementar exactamente un port.out, "
                                    + "pero implementa "
                                    + outputPorts.size()
                                    + ": "
                                    + outputPorts.stream()
                                    .map(JavaClass::getName)
                                    .toList()
                    )
            );
        }
    }
}
