package pe.ask.auth.architecture.core.usecase;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.reactivestreams.Publisher;

final class PublicMethodsMustBeReactiveCondition
        extends ArchCondition<JavaClass> {

    PublicMethodsMustBeReactiveCondition() {
        super("expose only reactive public methods");
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        for (JavaMethod method : item.getMethods()) {

            if (!method.getOwner().equals(item)) {
                continue;
            }

            boolean publicMethod =
                    method.getModifiers()
                            .contains(JavaModifier.PUBLIC);

            boolean staticMethod =
                    method.getModifiers()
                            .contains(JavaModifier.STATIC);

            if (!publicMethod || staticMethod) {
                continue;
            }

            JavaClass returnType =
                    method.getRawReturnType();

            if (!returnType.isAssignableTo(Publisher.class)) {
                events.add(
                        SimpleConditionEvent.violated(
                                item,
                                method.getFullName()
                                        + " retorna "
                                        + returnType.getName()
                                        + "; toda API pública del use case "
                                        + "debe retornar Mono, Flux o Publisher"
                        )
                );
            }
        }
    }
}
