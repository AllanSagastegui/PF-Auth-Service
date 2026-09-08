package pe.ask.auth.core.usecase.annotation;

import java.lang.annotation.*;

/**
 * Custom annotation to mark classes as Use Cases within the application.
 * <p>
 * This annotation serves as a stereotype for identifying and organizing the
 * application's use case implementations. It can be used by frameworks or
 * tools for component scanning, dependency injection, or documentation generation.
 * </p>
 *
 * @author Allan Sagastegui
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseCase {
}
