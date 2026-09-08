package pe.ask.auth.architecture.app;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import reactor.core.scheduler.Schedulers;

import java.net.http.HttpClient;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.BaseStream;

final class ForbiddenReactiveCallsCondition
        extends ArchCondition<JavaClass> {

    private static final Set<String> REACTOR_FORBIDDEN_METHODS = Set.of(
            "block",
            "blockOptional",
            "blockFirst",
            "blockLast",
            "toIterable",
            "toStream",
            "toFuture",
            "subscribe",
            "subscribeWith"
    );

    private static final Set<String> FORBIDDEN_SCHEDULER_METHODS = Set.of(
            "boundedElastic",
            "newBoundedElastic",
            "fromExecutor",
            "fromExecutorService"
    );

    private static final Set<String> EXECUTOR_BLOCKING_METHODS = Set.of(
            "execute",
            "submit",
            "invokeAll",
            "invokeAny",
            "awaitTermination"
    );

    ForbiddenReactiveCallsCondition() {
        super(
                "not call blocking, imperative or manually subscribing APIs"
        );
    }

    @Override
    public void check(
            JavaClass item,
            ConditionEvents events
    ) {
        for (JavaMethodCall call : item.getMethodCallsFromSelf()) {
            String reason = findViolationReason(call);

            if (reason != null) {
                events.add(
                        SimpleConditionEvent.violated(
                                item,
                                call.getDescription()
                                        + " está prohibido porque "
                                        + reason
                        )
                );
            }
        }
    }

    private String findViolationReason(JavaMethodCall call) {
        String ownerName = call.getTargetOwner().getName();
        String methodName = call.getName();

        /*
         * Reactor: bloqueos, extracción imperativa y subscriptions manuales.
         */
        if (ownerName.startsWith("reactor.core.publisher.")
                && REACTOR_FORBIDDEN_METHODS.contains(methodName)) {
            return "rompe la composición reactiva o realiza "
                    + "una subscription manual";
        }

        /*
         * boundedElastic permite esconder APIs bloqueantes.
         *
         * En este proyecto se aceptan únicamente drivers no bloqueantes.
         */
        if (ownerName.equals(Schedulers.class.getName())
                && FORBIDDEN_SCHEDULER_METHODS.contains(methodName)) {
            return "no se permite ocultar operaciones bloqueantes "
                    + "en boundedElastic o executors externos";
        }

        /*
         * Future.get() y CompletableFuture.join().
         */
        if (call.getTargetOwner().isAssignableTo(Future.class)
                && methodName.equals("get")) {
            return "Future.get() espera sincrónicamente un resultado";
        }

        if (call.getTargetOwner()
                .isAssignableTo(CompletableFuture.class)
                && methodName.equals("join")) {
            return "CompletableFuture.join() espera sincrónicamente";
        }

        if (ownerName.equals(CompletableFuture.class.getName())
                && Set.of("runAsync", "supplyAsync")
                .contains(methodName)) {
            return "la concurrencia debe administrarse mediante Reactor";
        }

        /*
         * Threads manuales.
         */
        if (call.getTargetOwner().isAssignableTo(Thread.class)
                && Set.of("sleep", "join", "start")
                .contains(methodName)) {
            return "no deben administrarse threads manualmente";
        }

        /*
         * Pools manuales.
         */
        if (ownerName.equals(Executors.class.getName())
                && methodName.startsWith("new")) {
            return "no deben construirse ExecutorService manualmente";
        }

        if (call.getTargetOwner()
                .isAssignableTo(ExecutorService.class)
                && EXECUTOR_BLOCKING_METHODS.contains(methodName)) {
            return "ExecutorService no forma parte del pipeline Reactor";
        }

        /*
         * Locks y primitivas de sincronización.
         */
        if (ownerName.equals(LockSupport.class.getName())
                && methodName.startsWith("park")) {
            return "LockSupport.park bloquea el thread";
        }

        if (call.getTargetOwner().isAssignableTo(Lock.class)
                && (
                methodName.equals("lock")
                        || methodName.equals("lockInterruptibly")
                        || (
                        methodName.equals("tryLock")
                                && hasParameter(call)
                )
        )) {
            return "los locks bloqueantes no deben utilizarse "
                    + "en un pipeline reactivo";
        }

        if (call.getTargetOwner()
                .isAssignableTo(CountDownLatch.class)
                && methodName.equals("await")) {
            return "CountDownLatch.await bloquea el thread";
        }

        if (call.getTargetOwner()
                .isAssignableTo(CyclicBarrier.class)
                && methodName.equals("await")) {
            return "CyclicBarrier.await bloquea el thread";
        }

        if (call.getTargetOwner().isAssignableTo(Phaser.class)
                && methodName.startsWith("await")) {
            return "Phaser.await bloquea el thread";
        }

        if (call.getTargetOwner().isAssignableTo(Semaphore.class)
                && (
                methodName.equals("acquire")
                        || methodName.equals("acquireUninterruptibly")
                        || (
                        methodName.equals("tryAcquire")
                                && hasParameter(call)
                )
        )) {
            return "Semaphore puede bloquear el thread";
        }

        if (call.getTargetOwner()
                .isAssignableTo(BlockingQueue.class)
                && isBlockingQueueOperation(call)) {
            return "BlockingQueue puede suspender el thread";
        }

        /*
         * Streams paralelos utilizan concurrencia fuera de Reactor.
         */
        if (methodName.equals("parallelStream")) {
            return "parallelStream utiliza concurrencia fuera de Reactor";
        }

        if (call.getTargetOwner().isAssignableTo(BaseStream.class)
                && methodName.equals("parallel")) {
            return "Stream.parallel utiliza concurrencia fuera de Reactor";
        }

        /*
         * HTTP sincrónico del JDK.
         */
        if (call.getTargetOwner().isAssignableTo(HttpClient.class)
                && methodName.equals("send")) {
            return "HttpClient.send es una llamada HTTP síncrona";
        }

        /*
         * OkHttp sincrónico.
         */
        if (ownerName.equals("okhttp3.Call")
                && methodName.equals("execute")) {
            return "OkHttp Call.execute es síncrono";
        }

        /*
         * Procesos del sistema.
         */
        if (call.getTargetOwner().isAssignableTo(Process.class)
                && methodName.equals("waitFor")) {
            return "Process.waitFor bloquea el thread";
        }

        if (ownerName.equals(ProcessBuilder.class.getName())
                && methodName.equals("start")) {
            return "no deben iniciarse procesos del sistema";
        }

        if (ownerName.equals(Runtime.class.getName())
                && Set.of("exec", "halt").contains(methodName)) {
            return "no deben ejecutarse procesos ni detener la JVM";
        }

        if (ownerName.equals(System.class.getName())
                && methodName.equals("exit")) {
            return "el código productivo no debe finalizar la JVM";
        }

        return null;
    }

    private boolean isBlockingQueueOperation(
            JavaMethodCall call
    ) {
        String methodName = call.getName();

        if (methodName.equals("take")
                || methodName.equals("put")) {
            return true;
        }

        if (methodName.equals("poll")
                || methodName.equals("offer")) {
            return hasParameter(call);
        }

        return false;
    }

    private boolean hasParameter(
            JavaMethodCall call
    ) {
        return call.getTarget()
                .getRawParameterTypes()
                .stream()
                .anyMatch(javaClass ->
                        javaClass.isEquivalentTo(TimeUnit.class)
                );
    }
}
