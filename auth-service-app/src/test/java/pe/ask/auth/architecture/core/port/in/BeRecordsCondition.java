package pe.ask.auth.architecture.core.port.in;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public final class BeRecordsCondition
        extends ArchCondition<JavaClass> {

    public BeRecordsCondition() {
        super("be records");
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        if (!item.isRecord()) {
            events.add(
                    SimpleConditionEvent.violated(
                            item,
                            item.getName()
                                    + " debe declararse como record"
                    )
            );
        }
    }
}
