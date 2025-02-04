package fast_reset.client.completefutures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public final class AsyncSupply<T> implements Runnable {
    private CompletableFuture<T> future;
    private Supplier<? extends T> supplier;

    private AsyncSupply(CompletableFuture<T> future, Supplier<? extends T> supplier) {
        this.future = future;
        this.supplier = supplier;
    }

    @Override
    public void run() {
        CompletableFuture<T> f;
        Supplier<? extends T> s;
        if ((f = future) != null && (s = supplier) != null) {
            future = null;
            supplier = null;
            if (!f.isDone()) {
                try {
                    f.complete(s.get());
                } catch (Throwable ex) {
                    f.completeExceptionally(ex);
                }
            }
        }
    }

    public void cancel() {
        this.future.complete(null);
        this.future = null;
    }

    /**
     * Copy of {@link CompletableFuture#supplyAsync(Supplier, Executor)}, using our own cancellable version of AsyncSupply.
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        CompletableFuture<U> future = new CompletableFuture<>();
        executor.execute(new AsyncSupply<>(future, supplier));
        return future;
    }

    /**
     * Copy of {@link CompletableFuture#thenApplyAsync(Function, Executor)}, using our own cancellable version of AsyncSupply.
     */
    public static <T, U> CompletableFuture<U> thenApplyAsync(CompletableFuture<T> from, Function<T, U> function, Executor executor) {
        CompletableFuture<U> future = new CompletableFuture<>();
        from.thenAccept(result -> executor.execute(new AsyncSupply<>(future, () -> function.apply(result)))).exceptionally(throwable -> {
            future.completeExceptionally(throwable);
            return null;
        });
        return future;
    }

    /**
     * Copy of {@link CompletableFuture#exceptionallyAsync(Function, Executor)}, using our own cancellable version of AsyncSupply.
     */
    public static <T> CompletableFuture<T> exceptionallyAsync(CompletableFuture<T> from, Function<Throwable, T> function, Executor executor) {
        CompletableFuture<T> future = new CompletableFuture<>();
        from.thenAccept(future::complete).exceptionally(throwable -> {
            executor.execute(new AsyncSupply<>(future, () -> function.apply(throwable)));
            return null;
        });
        return future;
    }
}