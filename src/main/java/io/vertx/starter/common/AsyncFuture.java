package io.vertx.starter.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author vishwa161
 */
public class AsyncFuture<T> extends CompletableFuture<T> {

  private static final Executor ioPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private static final Executor taskPool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

  public static <T> CompletableFuture<T> executeIOBlocking(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier, ioPool);
  }

  public static <T> CompletableFuture<T> executeBlocking(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier, taskPool);
  }


}
