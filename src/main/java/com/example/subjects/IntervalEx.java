package com.example.subjects;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IntervalEx {
    public static void main(String[] args) {
        Publisher<Integer> pub = sub -> {
            sub.onSubscribe(new Subscription() {
                int no = 0;
                boolean cancelled = false;

                @Override
                public void request(long l) {
                    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                    exec.scheduleAtFixedRate(() -> {
                        if (cancelled) {
                            exec.shutdown();
                            return;
                        }
                        sub.onNext(no++);
                    }, 0, 300, TimeUnit.MILLISECONDS);
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        };

        Publisher<Integer> takePub = sub -> {
            pub.subscribe(new Subscriber<Integer>() {
                int count = 0;
                Subscription subc;

                @Override
                public void onSubscribe(Subscription subscription) {
                    subc = subscription;
                    sub.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer integer) {
                    sub.onNext(integer);
                    if (++count > 5) {
                        subc.cancel();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    sub.onError(throwable);
                }

                @Override
                public void onComplete() {
                    sub.onComplete();
                }
            });
        };

        takePub.subscribe(new Subscriber<Integer>() {
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
    }
}
