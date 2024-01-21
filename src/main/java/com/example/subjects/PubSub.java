package com.example.subjects;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;

public class PubSub {

    public static void main(String[] args) throws InterruptedException {
        // Publisher <- Observable
        // Subscriber <- Observer

        List<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);
        ExecutorService es = Executors.newCachedThreadPool();

        Publisher p = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                // publisher 는 subscriber 가 subscribe 할 때마다 subscription 을 만들어준다.
                // subscription 은 subscriber 가 back pressure 를 통해 publisher 에게 요청할 수 있는 것들을 정의해놓은 것이다.
                Iterator<Integer> it = itr.iterator();
                subscriber.onSubscribe(new Subscription() {
                    @Override
                    // subscriber 가 처리할 수 있는 아이템의 수를 요청
                    public void request(long n) {
                        es.execute(() -> {
                            int i = 0;
                            try {
                                while (i++ < n) {
                                    if (it.hasNext()) {
                                        subscriber.onNext(it.next());
                                    } else {
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> s = new Subscriber<Integer>() {
            Subscription subscription;
            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println("PubSub.onSubscribe");
//                subscription.request(10);
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            // 다음 아이템을 호출1
            // reactor 에서는 buffer 하나 두고 back pressure 처리
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + " onNext " + item);
                this.subscription.request(1);
            }

            @Override
            // Exception 발생
            public void onError(Throwable throwable) {
                System.out.println("PubSub.onError " + throwable);
            }

            @Override
            // 아이템을 다 처리했음을 알림
            public void onComplete() {
                System.out.println("PubSub.onComplete");
            }
        };

        p.subscribe(s);
        es.awaitTermination(10, TimeUnit.SECONDS);
        es.shutdown();
    }
}
