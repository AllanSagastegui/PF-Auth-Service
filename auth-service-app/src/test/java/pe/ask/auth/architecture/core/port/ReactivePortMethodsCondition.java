package pe.ask.auth.architecture.core.port;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.reactivestreams.Publisher;

public final class ReactivePortMethodsCondition
        extends ArchCondition<JavaClass> {

    public ReactivePortMethodsCondition() {
        super("declare exclusively reactive port methods");
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        for (JavaMethod method : item.getMethods()) {

            /*
             * Solamente métodos declarados directamente por
             * la interfaz que estamos analizando.
             */
            if (!method.getOwner().equals(item)) {
                continue;
            }

            validateMethodIsAbstract(
                    item,
                    method,
                    events
            );

            validateReactiveReturnType(
                    item,
                    method,
                    events
            );

            validateNoDeclaredExceptions(
                    item,
                    method,
                    events
            );
        }
    }

    private void validateMethodIsAbstract(
            JavaClass owner,
            JavaMethod method,
            ConditionEvents events
    ) {
        boolean abstractMethod =
                method.getModifiers()
                        .contains(JavaModifier.ABSTRACT);

        if (!abstractMethod) {
            events.add(
                    SimpleConditionEvent.violated(
                            owner,
                            method.getFullName()
                                    + " debe ser un método abstracto; "
                                    + "los ports no deben contener "
                                    + "implementaciones default o static"
                    )
            );
        }
    }

    private void validateReactiveReturnType(
            JavaClass owner,
            JavaMethod method,
            ConditionEvents events
    ) {
        JavaClass returnType =
                method.getRawReturnType();

        boolean reactive =
                returnType.isAssignableTo(Publisher.class);

        if (!reactive) {
            events.add(
                    SimpleConditionEvent.violated(
                            owner,
                            method.getFullName()
                                    + " retorna "
                                    + returnType.getName()
                                    + ", pero todo port debe retornar "
                                    + "Mono, Flux o Publisher"
                    )
            );
        }
    }

    private void validateNoDeclaredExceptions(
            JavaClass owner,
            JavaMethod method,
            ConditionEvents events
    ) {
        if (method.getThrowsClause()
                .iterator()
                .hasNext()) {

            events.add(
                    SimpleConditionEvent.violated(
                            owner,
                            method.getFullName()
                                    + " declara excepciones mediante throws; "
                                    + "los errores deben viajar por el "
                                    + "error signal del Publisher"
                    )
            );
        }
    }
}
