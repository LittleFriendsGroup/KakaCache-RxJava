package com.im4j.kakacache.rxjava.netcache.retrofit;

import com.im4j.kakacache.rxjava.KakaCache;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.netcache.ResultData;
import com.im4j.kakacache.rxjava.netcache.strategy.CacheStrategy;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

import okio.ByteString;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.Result;
import rx.Observable;
import rx.Producer;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.exceptions.Exceptions;

/**
 * 支持缓存的RxJavaCallAdapterFactory
 * @version alafighting 2016-07
 */
public class KakaRxCallAdapterFactory extends CallAdapter.Factory {

    public static KakaRxCallAdapterFactory create() {
        return new KakaRxCallAdapterFactory(null);
    }

    public static KakaRxCallAdapterFactory createWithScheduler(Scheduler scheduler) {
        if (scheduler == null) throw new NullPointerException("scheduler == null");
        return new KakaRxCallAdapterFactory(scheduler);
    }

    private final Scheduler scheduler;

    private KakaRxCallAdapterFactory(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);
        String canonicalName = rawType.getCanonicalName();
        boolean isSingle = "rx.Single".equals(canonicalName);
        boolean isCompletable = "rx.Completable".equals(canonicalName);
        if (rawType != Observable.class && !isSingle && !isCompletable) {
            return null;
        }
        if (!isCompletable && !(returnType instanceof ParameterizedType)) {
            String name = isSingle ? "Single" : "Observable";
            throw new IllegalStateException(name + " return type must be parameterized"
                    + " as " + name + "<Foo> or " + name + "<? extends Foo>");
        }

        if (isCompletable) {
            return CompletableHelper.createCallAdapter(scheduler);
        }

        CallAdapter<Observable<?>> callAdapter = getCallAdapter(returnType, annotations, scheduler);
        if (isSingle) {
            return SingleHelper.makeSingle(callAdapter);
        }
        return callAdapter;
    }

    private CallAdapter<Observable<?>> getCallAdapter(Type returnType, Annotation[] annotations, Scheduler scheduler) {
        return new CacheCallAdapter(annotations, getRealCallAdapter(returnType, scheduler));
    }
    private static final class CacheCallAdapter implements CallAdapter<Observable<?>> {
        private CustomCallAdapter callAdapter;
        private Annotation[] annotations;

        public CacheCallAdapter(Annotation[] annotations, CustomCallAdapter callAdapter) {
            this.annotations = annotations;
            this.callAdapter = callAdapter;
        }

        @Override
        public Type responseType() {
            return callAdapter.responseType();
        }

        @Override
        public <R> Observable<?> adapt(Call<R> call) {
            // 返回值类型为ResultData<T>
            if (ResultData.class.equals(Utils.getRawType(responseType()))) {
                // 启用缓存
                CacheInfo info = CacheInfo.get(annotations);
                LogUtils.log(info);
                if (info.isEnable()) {
                    // 处理缓存

                    if (Utils.isEmpty(info.getKey())) {
                        // 生成KEY
                        String key = Utils.buildKey(call.request());
                        LogUtils.log("buildKey="+key);
                        info.setKey(ByteString.of(key.getBytes()).md5().hex());
                    }

                    LogUtils.log("fileName="+info.getKey());

                    if (info.getStrategy() == null) {
                        info.setStrategy(CacheStrategy.FirstCache);
                    }

                    return callAdapter.doAdaptUnwrap(call)
                            .compose(KakaCache.transformer(info.getKey(), info.getStrategy()));
                }

                return callAdapter.doAdaptUnwrap(call);
            }

            return callAdapter.adapt(call);
        }
    }

    private CustomCallAdapter getRealCallAdapter(Type returnType, Scheduler scheduler) {
        Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Class<?> rawObservableType = getRawType(observableType);
        if (rawObservableType == Response.class) {
            if (!(observableType instanceof ParameterizedType)) {
                throw new IllegalStateException("Response must be parameterized"
                        + " as Response<Foo> or Response<? extends Foo>");
            }
            Type responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
            return new ResponseCallAdapter(responseType, scheduler);
        }

        if (rawObservableType == Result.class) {
            if (!(observableType instanceof ParameterizedType)) {
                throw new IllegalStateException("Result must be parameterized"
                        + " as Result<Foo> or Result<? extends Foo>");
            }
            Type responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
            return new ResultCallAdapter(responseType, scheduler);
        }

        return new SimpleCallAdapter(observableType, scheduler);
    }

    static final class CallOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
        private final Call<T> originalCall;

        CallOnSubscribe(Call<T> originalCall) {
            this.originalCall = originalCall;
        }

        @Override public void call(final Subscriber<? super Response<T>> subscriber) {
            // Since Call is a one-shot type, clone it for each new subscriber.
            Call<T> call = originalCall.clone();

            // Wrap the call in a helper which handles both unsubscription and backpressure.
            RequestArbiter<T> requestArbiter = new RequestArbiter<>(call, subscriber);
            subscriber.add(requestArbiter);
            subscriber.setProducer(requestArbiter);
        }
    }

    static final class RequestArbiter<T> extends AtomicBoolean implements Subscription, Producer {
        private final Call<T> call;
        private final Subscriber<? super Response<T>> subscriber;

        RequestArbiter(Call<T> call, Subscriber<? super Response<T>> subscriber) {
            this.call = call;
            this.subscriber = subscriber;
        }

        @Override public void request(long n) {
            if (n < 0) throw new IllegalArgumentException("n < 0: " + n);
            if (n == 0) return; // Nothing to do when requesting 0.
            if (!compareAndSet(false, true)) return; // Request was already triggered.

            try {
                Response<T> response = call.execute();
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(response);
                }
            } catch (Throwable t) {
                Exceptions.throwIfFatal(t);
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(t);
                }
                return;
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }

        @Override public void unsubscribe() {
            call.cancel();
        }

        @Override public boolean isUnsubscribed() {
            return call.isCanceled();
        }
    }

    static final class ResponseCallAdapter extends CustomCallAdapter {

        ResponseCallAdapter(Type responseType, Scheduler scheduler) {
            super(responseType, scheduler);
        }

        @Override public <R> Observable<Response<R>> adapt(Call<R> call) {
            Observable<Response<R>> observable = Observable.create(new CallOnSubscribe<>(call));
            if (scheduler != null) {
                return observable.subscribeOn(scheduler);
            }
            return observable;
        }

        @Override
        public <R> Observable<?> adaptUnwrap(Call<ResultData<R>> call) {
            Observable<Response<R>> observable = Observable.create(new CustomCallOnSubscribe<>(call));
            if (scheduler != null) {
                return observable.subscribeOn(scheduler);
            }
            return observable;
        }
    }

    static final class SimpleCallAdapter extends CustomCallAdapter {

        SimpleCallAdapter(Type responseType, Scheduler scheduler) {
            super(responseType, scheduler);
        }

        @Override public <R> Observable<R> adapt(Call<R> call) {
            Observable<R> observable = Observable.create(new CallOnSubscribe<>(call)) //
                    .lift(OperatorMapResponseToBodyOrError.<R>instance());
            if (scheduler != null) {
                return observable.subscribeOn(scheduler);
            }
            return observable;
        }
        @Override public <R> Observable<R> adaptUnwrap(Call<ResultData<R>> call) {
            Observable<R> observable = Observable.create(new CustomCallOnSubscribe<>(call)) //
                    .lift(OperatorMapResponseToBodyOrError.<R>instance());
            if (scheduler != null) {
                return observable.subscribeOn(scheduler);
            }
            return observable;
        }
    }

    static final class ResultCallAdapter extends CustomCallAdapter {
        ResultCallAdapter(Type responseType, Scheduler scheduler) {
            super(responseType, scheduler);
        }

        @Override public <R> Observable<Result<R>> adapt(Call<R> call) {
            Observable<Result<R>> observable = Observable.create(new CallOnSubscribe<>(call)) //
                    .map(response -> Result.response(response))
                    .onErrorReturn(throwable -> Result.error(throwable));
            if (scheduler != null) {
                return observable.subscribeOn(scheduler);
            }
            return observable;
        }

        @Override
        public <R> Observable<?> adaptUnwrap(Call<ResultData<R>> call) {
            Observable<Result<R>> observable = Observable.create(new CustomCallOnSubscribe<>(call)) //
                    .map(response -> Result.response(response))
                    .onErrorReturn(throwable -> Result.error(throwable));
            if (scheduler != null) {
                return observable.subscribeOn(scheduler);
            }
            return observable;
        }
    }

    static abstract class CustomCallAdapter implements CallAdapter<Observable<?>> {
        private final Type responseType;
        final Scheduler scheduler;

        CustomCallAdapter(Type responseType, Scheduler scheduler) {
            this.responseType = responseType;
            this.scheduler = scheduler;
        }

        @Override public Type responseType() {
            return responseType;
        }

        public <R> Observable<?> doAdaptUnwrap(Call<R> call) {
            return adaptUnwrap((Call<ResultData<R>>) call);
        }
        public abstract <R> Observable<?> adaptUnwrap(Call<ResultData<R>> call);
    }

    static final class CustomCallOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
        private final Call<ResultData<T>> originalCall;

        CustomCallOnSubscribe(Call<ResultData<T>> originalCall) {
            this.originalCall = originalCall;
        }

        @Override public void call(final Subscriber<? super Response<T>> subscriber) {
            // Since Call is a one-shot type, clone it for each new subscriber.
            Call<ResultData<T>> call = originalCall.clone();

            // Wrap the call in a helper which handles both unsubscription and backpressure.
            CustomRequestArbiter<T> requestArbiter = new CustomRequestArbiter<>(call, subscriber);
            subscriber.add(requestArbiter);
            subscriber.setProducer(requestArbiter);
        }
    }

    static final class CustomRequestArbiter<T> extends AtomicBoolean implements Subscription, Producer {
        private final Call<ResultData<T>> call;
        private final Subscriber<? super Response<T>> subscriber;

        CustomRequestArbiter(Call<ResultData<T>> call, Subscriber<? super Response<T>> subscriber) {
            this.call = call;
            this.subscriber = subscriber;
        }

        @Override public void request(long n) {
            if (n < 0) throw new IllegalArgumentException("n < 0: " + n);
            if (n == 0) return; // Nothing to do when requesting 0.
            if (!compareAndSet(false, true)) return; // Request was already triggered.

            try {
                Response<ResultData<T>> resultData = call.execute();
                Response<T> response = Response.success(resultData.body().data, resultData.raw());
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(response);
                }
            } catch (Throwable t) {
                Exceptions.throwIfFatal(t);
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(t);
                }
                return;
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }

        @Override public void unsubscribe() {
            call.cancel();
        }

        @Override public boolean isUnsubscribed() {
            return call.isCanceled();
        }
    }

}
