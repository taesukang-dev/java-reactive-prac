import org.reactivestreams.Subscriber;

public class DelegateSub<T, R> implements Subscriber<T> {
    Subscriber sub;

    public DelegateSub(Subscriber<? super R> sub) {
        this.sub = sub;
    }

    @Override
    public void onSubscribe(org.reactivestreams.Subscription subscription) {
        sub.onSubscribe(subscription);
    }

    @Override
    public void onNext(T t) {
        sub.onNext(t);
    }

    @Override
    public void onError(Throwable throwable) {
        sub.onError(throwable);
    }

    @Override
    public void onComplete() {
        sub.onComplete();
    }
}
