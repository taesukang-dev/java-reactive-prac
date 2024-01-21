package com.example.subjects;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SchedulerEx {
    public static void main(String[] args) {
        Publisher<Integer> pub = sub -> {

             sub.onSubscribe(new Subscription() {
                 @Override
                 public void request(long l) {
                     System.out.println(Thread.currentThread().getName() + " SchedulerEx.request\n");
                     sub.onNext(1);
                     sub.onNext(2);
                     sub.onNext(3);
                     sub.onNext(4);
                     sub.onNext(5);
                     sub.onComplete();
                 }

                 @Override
                 public void cancel() {

                 }
             });

        };

//        Publisher<Integer> subOnPub = sub -> {
//            ExecutorService es = Executors.newSingleThreadExecutor();
//            es.execute(() -> pub.subscribe(sub));
            // 여기에서 sub 은 subOnPub.subscribe(...) 를 호출할 때 만들어진다.
            // 함수형 프로그래밍이기 때문에 함수 자체를 참조값으로 가지고 가게 되고
            // subOnPub 이 subscribe 를 호출할 때 10번 라인부터 순차적으로 실행되고
            // 결국 sub 이 만들어져 순차적인 실행이 가능하다
            // 이러한 개념은 lazy evaluation or lazy execution 이라고 한다.
            // 즉, subOnPub.subscribe(...) 가 호출되는 시점에
            // sub 가 실제 Subscriber 로 바뀌고, 그 시점에서 subOnPub 내부의 코드가 실행된다.
//        };


//        subOnPub.subscribe(new Subscriber<Integer>() {
//            @Override
//            public void onSubscribe(Subscription s) {
//                System.out.println(Thread.currentThread().getName() + " SchedulerEx.onSubscribe");
//                s.request(Long.MAX_VALUE);
//            }
//
//            @Override
//            public void onNext(Integer integer) {
//                System.out.println(Thread.currentThread().getName() + " SchedulerEx.onNext " + integer);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                System.out.println(Thread.currentThread().getName() + " SchedulerEx.onError");
//            }
//
//            @Override
//            public void onComplete() {
//                System.out.println(Thread.currentThread().getName() + " SchedulerEx.onComplete");
//            }
//        });

        // publishOn 은 데이터 생성은 빠르지만, 데이터의 전달은 느린 경우 사용한다
        // subscriber 가 consume 하는게 느린 경우
        Publisher<Integer> pubOnPub = sub -> {
            pub.subscribe(new Subscriber<Integer>() {
                ExecutorService es = Executors.newSingleThreadExecutor();
                @Override
                public void onSubscribe(Subscription subscription) {
                    sub.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer integer) {
                    es.execute(() -> sub.onNext(integer));
                }

                @Override
                public void onError(Throwable throwable) {
                    es.execute(() -> sub.onError(throwable));
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    es.execute(() -> sub.onComplete());
                    es.shutdown();
                }
            });
        };

        pubOnPub.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println(Thread.currentThread().getName() + " SchedulerEx.onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(Thread.currentThread().getName() + " SchedulerEx.onNext " + integer);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(Thread.currentThread().getName() + " SchedulerEx.onError");
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " SchedulerEx.onComplete");
            }
        });

        System.out.println("exit\n");
    }
}
