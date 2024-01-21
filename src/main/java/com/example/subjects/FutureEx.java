package com.example.subjects;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class FutureEx {
    interface SuccessCallback {
        void onSuccess(String result);
    }

    interface ExceptionCallback {
        void onError(Throwable t);
    }

    public static class CallbackFutureTast extends FutureTask<String> {
        SuccessCallback sc;
        ExceptionCallback ec;
        public CallbackFutureTast(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
            super(callable);
            this.sc = Objects.requireNonNull(sc);
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            try {
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                ec.onError(e.getCause());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newCachedThreadPool();

        CallbackFutureTast f = new CallbackFutureTast(() -> {
            Thread.sleep(2000);
            if (1 == 1) throw new RuntimeException("Async Error!!");
            log.info("Async");
            return "Hello";
        }, result -> System.out.println("Result: " + result),
                e -> System.out.println("Error: " + e.getMessage())
        );

//        FutureTask<String> f = new FutureTask<String>(() -> {
//            Thread.sleep(2000);
//            log.info("Async");
//            return "Hello";
//        }) {
//            @Override
//            protected void done() {
//                try {
//                    System.out.println(get());
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                } catch (ExecutionException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        };

        es.execute(f);
        es.shutdown();

//        Future<String> f = es.submit(() -> {
//            Thread.sleep(2000);
//            log.info("Async");
//            return "Hello";
//        });

//        System.out.println(f.isDone());
//        Thread.sleep(2100);
//        System.out.println("Exit");
//
//        System.out.println(f.isDone());
//        System.out.println(f.get()); // 결과가 올 때까지 blocking
    }
}
