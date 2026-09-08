package pe.ask.auth.architecture.app;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.beans.factory.BeanFactory;

import java.util.Set;

final class NoServiceLocatorCallsCondition
        extends ArchCondition<JavaClass> {

    private static final Set<String> FORBIDDEN_METHODS = Set.of(
            "getBean",
            "getBeansOfType",
            "getBeanNamesForType",
            "getBeanProvider"
    );

    NoServiceLocatorCallsCondition() {
        super("not use Spring BeanFactory as a service locator");
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        for (JavaMethodCall call : item.getMethodCallsFromSelf()) {
            boolean beanFactoryCall =
                    call.getTargetOwner()
                            .isAssignableTo(BeanFactory.class);

            boolean forbiddenMethod =
                    FORBIDDEN_METHODS.contains(call.getName());

            if (beanFactoryCall && forbiddenMethod) {
                events.add(
                        SimpleConditionEvent.violated(
                                item,
                                call.getDescription()
                                        + " utiliza el contenedor Spring "
                                        + "como service locator"
                        )
                );
            }
        }
    }
}
