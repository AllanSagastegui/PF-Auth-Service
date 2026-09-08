package pe.ask.auth.architecture.output.producer;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static pe.ask.auth.architecture.ArchitecturePackages.*;

public final class ProducerArchitectureRules {

    private ProducerArchitectureRules() {
        throw new IllegalStateException("Architecture rules class");
    }

    /**
     * Producer / Kafka messaging solamente puede contener:
     * - adapter
     * - message
     * - mapper
     */
    @ArchTest
    static final ArchRule PRODUCER_MUST_ONLY_CONTAIN_ALLOWED_AREAS =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA,
                            OUTPUT_PRODUCER
                    )
                    .should()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA_ADAPTER,
                            OUTPUT_PRODUCER_ADAPTER,
                            OUTPUT_KAFKA_MESSAGE,
                            OUTPUT_PRODUCER_MESSAGE,
                            OUTPUT_KAFKA_MAPPER,
                            OUTPUT_PRODUCER_MAPPER
                    )
                    .because(
                            "el output producer/kafka solamente debe contener "
                                    + "adapter, message y mapper"
                    );

    /**
     * No permitimos Kafka consumers en el módulo output producer.
     * Un consumer es un adapter de entrada.
     */
    @ArchTest
    static final ArchRule PRODUCER_OUTPUT_MUST_NOT_USE_CONSUMER_API =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA,
                            OUTPUT_PRODUCER
                    )
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.apache.kafka.clients.consumer..",
                            "reactor.kafka.receiver.."
                    )
                    .because(
                            "output.kafka-producer solamente publica mensajes; "
                                    + "un consumer es un adapter de entrada"
                    );

    /**
     * Prohibimos KafkaTemplate imperativo para mantener un contrato Reactor.
     */
    @ArchTest
    static final ArchRule IMPERATIVE_KAFKA_TEMPLATE_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA,
                            OUTPUT_PRODUCER
                    )
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName(
                            "org.springframework.kafka.core.KafkaTemplate"
                    )
                    .because(
                            "el producer debe mantener el pipeline Reactor "
                                    + "mediante ReactiveKafkaProducerTemplate o KafkaSender"
                    );

    /**
     * Prohibimos manipular el KafkaProducer síncrono/bloqueante directamente.
     */
    @ArchTest
    static final ArchRule RAW_KAFKA_PRODUCER_MUST_NOT_BE_USED =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA,
                            OUTPUT_PRODUCER
                    )
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName(
                            "org.apache.kafka.clients.producer.KafkaProducer"
                    )
                    .because(
                            "el adapter debe utilizar una abstracción Reactive Streams"
                    );

    @ArchTest
    static final ArchRule PRODUCER_ADAPTERS_MUST_END_WITH_ADAPTER =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA_ADAPTER,
                            OUTPUT_PRODUCER_ADAPTER
                    )
                    .should()
                    .haveSimpleNameEndingWith("Adapter")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule PRODUCER_ADAPTERS_MUST_BE_FINAL =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA_ADAPTER,
                            OUTPUT_PRODUCER_ADAPTER
                    )
                    .should()
                    .haveModifier(JavaModifier.FINAL)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule PRODUCER_MESSAGES_MUST_END_WITH_MESSAGE =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA_MESSAGE,
                            OUTPUT_PRODUCER_MESSAGE
                    )
                    .should()
                    .haveSimpleNameEndingWith("Message")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule PRODUCER_MAPPERS_MUST_END_WITH_MAPPER =
            classes()
                    .that()
                    .resideInAnyPackage(
                            OUTPUT_KAFKA_MAPPER,
                            OUTPUT_PRODUCER_MAPPER
                    )
                    .should()
                    .haveSimpleNameEndingWith("Mapper")
                    .allowEmptyShould(true);
}
