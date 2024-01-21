package com.example.subjects;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reactive Streams - Operators
 *
 * Publisher -> [Data1] -> mapPub (데이터 가공) -> [Data2] -> logSub
 * pub -> mapPub(onNext, apply function (* 10)) -> mapPub2(onNext, apply function (-)) -> logSub
 */
public class PubSub2 {
    public static void main(String[] args) {
        // 데이터의 발행은 여기에서 이뤄지지 않음, 실제로는 pub.subscribe 할 때 이뤄진다.
        // 따라서 아래 mapPub 에서 iterPub 을 subscribe 하면서 데이터가 발행된다.
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
//        Publisher<Integer> mapPub = mapPub(pub, (Function<Integer, Integer>) s -> s * 10);
//        Publisher<Integer> mapPub2 = mapPub(mapPub, (Function<Integer, Integer>) s -> -s);
//        Publisher<Integer> sumPub = sumPub(pub);
//        Publisher<String> reducePub = reducePub(pub, "", (a, b) -> a +"-"+ b);
        Publisher<StringBuilder> reducePub2 = reducePub(pub, new StringBuilder(), (a, b) -> a.append(b).append(","));

//        Publisher<Integer> mapPub_V2 = mapPub_v2(pub, (Function<Integer, Integer>) s -> s * 10);
//        Publisher<String> mapPub_V3 = mapPub_v3(pub, (Function<Integer, String>) s -> "[" + s + "]");

        reducePub2.subscribe(logSub());
    }

    private static <T, R> Publisher<R> mapPub_v3(Publisher<T> pub, Function<T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                // 데이터가 실제로 발행되는 trigger
                pub.subscribe(new DelegateSub<T, R>(sub) {
                    @Override
                    public void onNext(T integer) {
                        // 데이터를 가공하는 과정
                        sub.onNext(f.apply(integer));
                    }
                });
            }
        };
    }

    private static <T> Publisher<T> mapPub_v2(Publisher<T> pub, Function<T, T> f) {
        return new Publisher<T>() {
            @Override
            public void subscribe(Subscriber<? super T> sub) {
                // 데이터가 실제로 발행되는 trigger
                pub.subscribe(new DelegateSub<T, T>(sub) {
                    @Override
                    public void onNext(T integer) {
                        // 데이터를 가공하는 과정
                        sub.onNext(f.apply(integer));
                    }
                });
            }
        };
    }

    private static<T,R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R,T,R> bf) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> subscriber) {
                pub.subscribe(new DelegateSub<T,R>(subscriber) {
                    R result = init;
                    @Override
                    public void onNext(T integer) {
                        result = bf.apply(result, integer);
                    }

                    @Override
                    public void onComplete() {
                        subscriber.onNext(result);
                        subscriber.onComplete();
                    }
                });
            }
        };
    }


    private static Publisher<Integer> reducePub(Publisher<Integer> pub, int init, BiFunction<Integer, Integer, Integer> bf) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                pub.subscribe(new DelegateSub<Integer, Integer>(subscriber) {
                    int result = init;
                    @Override
                    public void onNext(Integer integer) {
                        result = bf.apply(result, integer);
                    }

                    @Override
                    public void onComplete() {
                        subscriber.onNext(result);
                        subscriber.onComplete();
                    }
                });
            }
        };
    }

    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                pub.subscribe(new DelegateSub<Integer, Integer>(subscriber) {
                    int sum = 0;
                    @Override
                    public void onNext(Integer integer) {
                        sum += integer;
                    }

                    @Override
                    public void onComplete() {
                        subscriber.onNext(sum);
                        subscriber.onComplete();
                    }
                });

            }

        };
    }

    private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> f) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                // 데이터가 실제로 발행되는 trigger
                pub.subscribe(new DelegateSub<Integer, Integer>(sub) {
                    @Override
                    public void onNext(Integer integer) {
                        // 데이터를 가공하는 과정
                        sub.onNext(f.apply(integer));
                    }
                });
            }
        };
    }

    private static<T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("PubSub2.onSubscribe");
                s.request(Long.MAX_VALUE);

            }

            @Override
            public void onNext(T integer) {
                System.out.println("onNext : " + integer);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("PubSub2.onError " + throwable);
            }

            @Override
            public void onComplete() {
                System.out.println("PubSub2.onComplete");
            }
        };
    }

    public static Publisher<Integer> iterPub(final List<Integer> iter) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try {
                            iter.forEach(sub::onNext);
                            sub.onComplete();
                        } catch (Throwable t) {
                            sub.onError(t);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }

        };

    }
}
