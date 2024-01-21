package com.example;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class Cfuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        CompletableFuture<Integer> f = new CompletableFuture<>();
////        f.complete(2); // complete 처리
////        System.out.println(f.get());
//
//        f.completeExceptionally(new RuntimeException()); // 예외로 complete
//        System.out.println(f.get());

//        CompletableFuture
//                .runAsync(() -> log.info("runAsync"))
//                .thenRun(() -> log.info("thenRun"))
//                .thenRun(() -> log.info("thenRun2"));

        ExecutorService es = Executors.newFixedThreadPool(10);

        CompletableFuture
                .supplyAsync(() -> {
                    log.info("runAsync");
//                    if (1==1) throw new RuntimeException();
                    return 1;
                })
                // thenCompose 는 return 값이 CompletableFuture, flatMap 과 같은 역할
                .thenCompose(s -> {
                    log.info("thenCompose {}", s);
                    return CompletableFuture.completedFuture(s + 1);
                })
                // thenApplyAsync 는 다음 작업을 다른 thread 에서 실행, 사용할 thread pool을 지정,
                // 다음 실행도 같은 thread pool 에서 실행
                .thenApplyAsync(s2 -> {
                    log.info("thenApply2 {}", s2);
                    return s2 + 1;
                }, es)
                .thenApply(s -> {
                    log.info("thenApply {}", s);
                    return s + 1;
                })
                // error 발생시 -10 리턴
                .exceptionally(e -> -10)
                .thenAccept(s2 -> log.info("thenAccept, {}", s2));
        log.info("exit");

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }
}
