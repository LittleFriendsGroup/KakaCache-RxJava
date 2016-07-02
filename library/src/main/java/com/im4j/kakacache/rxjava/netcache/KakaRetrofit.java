package com.im4j.kakacache.rxjava.netcache;

import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.netcache.retrofit.OkHttpCall;
import com.im4j.kakacache.rxjava.netcache.retrofit.ServiceMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 支持缓存的Retrofit
 * @version alafighting 2016-07
 */
public class KakaRetrofit {

    private Retrofit retrofit;
    private final Map<Method, ServiceMethod> serviceMethodCache = new LinkedHashMap<>();

    KakaRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    public <T> T create(final Class<T> service) {
        retrofit.create(service);

        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
                new InvocationHandler() {
                    @Override public Object invoke(Object proxy, Method method, Object... args)
                            throws Throwable {
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }

                        LogUtils.e(method);
                        LogUtils.e(method.getGenericReturnType());

                        ServiceMethod serviceMethod = loadServiceMethod(method);
                        OkHttpCall okHttpCall = new OkHttpCall<>(serviceMethod, args);
                        return serviceMethod.callAdapter.adapt(okHttpCall);
                    }
                });
    }

    private ServiceMethod loadServiceMethod(Method method) {
        ServiceMethod result;
        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder(this, method).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    public okhttp3.Call.Factory callFactory() {
        return retrofit.callFactory();
    }

    public HttpUrl baseUrl() {
        return retrofit.baseUrl();
    }

    public List<CallAdapter.Factory> callAdapterFactories() {
        return retrofit.callAdapterFactories();
    }

    public CallAdapter<?> callAdapter(Type returnType, Annotation[] annotations) {
        return retrofit.callAdapter(returnType, annotations);
    }

    public CallAdapter<?> nextCallAdapter(CallAdapter.Factory skipPast, Type returnType,
                                          Annotation[] annotations) {
        return retrofit.nextCallAdapter(skipPast, returnType, annotations);
    }

    public List<Converter.Factory> converterFactories() {
        return retrofit.converterFactories();
    }

    public <T> Converter<T, RequestBody> requestBodyConverter(Type type,
                                                              Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
        return retrofit.requestBodyConverter(type, parameterAnnotations, methodAnnotations);
    }

    public <T> Converter<T, RequestBody> nextRequestBodyConverter(Converter.Factory skipPast,
                                                                  Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
        return retrofit.nextRequestBodyConverter(skipPast, type, parameterAnnotations, methodAnnotations);
    }

    public <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
        return retrofit.responseBodyConverter(type, annotations);
    }

    public <T> Converter<ResponseBody, T> nextResponseBodyConverter(Converter.Factory skipPast,
                                                                    Type type, Annotation[] annotations) {
        return retrofit.nextResponseBodyConverter(skipPast, type, annotations);
    }

    public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        return retrofit.stringConverter(type, annotations);
    }

    public Executor callbackExecutor() {
        return retrofit.callbackExecutor();
    }



    public static final class Builder {
        private Retrofit.Builder builder;

        public Builder() {
            builder = new Retrofit.Builder();
        }

        public Builder client(OkHttpClient client) {
            builder.client(client);
            return this;
        }

        public Builder callFactory(okhttp3.Call.Factory factory) {
            this.builder.callFactory(factory);
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }

        public Builder baseUrl(HttpUrl baseUrl) {
            builder.baseUrl(baseUrl);
            return this;
        }

        public Builder addConverterFactory(Converter.Factory factory) {
            builder.addConverterFactory(factory);
            return this;
        }

        public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
            builder.addCallAdapterFactory(factory);
            return this;
        }

        public Builder callbackExecutor(Executor executor) {
            builder.callbackExecutor(executor);
            return this;
        }

        public Builder validateEagerly(boolean validateEagerly) {
            builder.validateEagerly(validateEagerly);
            return this;
        }

        public KakaRetrofit build() {
            Retrofit retrofit = builder.build();
            return new KakaRetrofit(retrofit);
        }
    }

}
