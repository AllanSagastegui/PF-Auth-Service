package pe.ask.auth.architecture.core.model;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class ImmutableDomainModelCondition
        extends ArchCondition<JavaClass> {

    ImmutableDomainModelCondition() {
        super("be immutable domain models");
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        /*
         * Interfaces, annotations y enums no necesitan
         * esta validación.
         */
        if (item.isInterface()
                || item.isAnnotation()
                || item.isEnum()) {
            return;
        }

        /*
         * Las excepciones pertenecientes al dominio pueden extender
         * RuntimeException y no deben ser tratadas como value objects.
         */
        if (item.isAssignableTo(Throwable.class)) {
            return;
        }

        boolean typeIsClosedForInheritance =
                item.isRecord()
                        || item.isSealed()
                        || item.getModifiers()
                        .contains(JavaModifier.FINAL);

        if (!typeIsClosedForInheritance) {
            events.add(
                    SimpleConditionEvent.violated(
                            item,
                            item.getName()
                                    + " debe ser record, final o sealed"
                    )
            );
        }

        for (JavaField field : item.getFields()) {

            boolean staticField =
                    field.getModifiers()
                            .contains(JavaModifier.STATIC);

            if (staticField) {
                continue;
            }

            boolean finalField =
                    field.getModifiers()
                            .contains(JavaModifier.FINAL);

            if (!finalField) {
                events.add(
                        SimpleConditionEvent.violated(
                                item,
                                field.getFullName()
                                        + " debe ser final"
                        )
                );
            }
        }
    }
}
