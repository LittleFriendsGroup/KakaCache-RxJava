package com.im4j.kakacache.rxjava.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.netcache.KakaCache;
import com.im4j.kakacache.rxjava.netcache.retrofit.KakaRxCallAdapterFactory;
import com.im4j.kakacache.rxjava.netcache.strategy.CacheAndRemoteStrategy;

import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Demo主界面
 * @version alafighting 2016-06
 */
public class MainActivity extends AppCompatActivity {
    static final String KEY_CACHE = "key_cache";

    private Button btnTestCache;
    private Button btnTestRetrofit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiity_main);

        btnTestCache = (Button) findViewById(R.id.btn_test_cache);
        btnTestCache.setOnClickListener(view -> {
            remote().compose(KakaCache.transformer(KEY_CACHE, new CacheAndRemoteStrategy())).subscribe(data -> {
                Log.e("main", "next  data=" + data);
            }, error -> {
                Log.e("main", "error", error);
            }, () -> {
                Log.e("main", "completed");
            });
        });

        btnTestRetrofit = (Button) findViewById(R.id.btn_test_retrofit);
        btnTestRetrofit.setOnClickListener(view -> {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.github.com/")
                    .addConverterFactory(GsonConverterFactory.create(KakaCache.gson().create()))
                    .addCallAdapterFactory(KakaRxCallAdapterFactory.create())
                    .build();

            GitHubService service = retrofit.create(GitHubService.class);

//            // 不修改原有代码，增加对Cache的支持
//            service.listRepos("octocat").compose(KakaCache.transformer(KEY_CACHE, new FirstCacheStrategy())).subscribeOn(Schedulers.io()).subscribe(data -> {
//                LogUtils.e(data);
//            }, error -> {
//                LogUtils.e(error);
//            });

            // 通过注解，自动支持Cache
            service.listRepos("octocat", "abc")
                    .subscribeOn(Schedulers.io())
                    .subscribe(data -> {
                LogUtils.e(data);
            }, error -> {
                LogUtils.e(error);
            });
        });
    }

    private rx.Observable<String> remote() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.e("main", "loadRemote");
                subscriber.onNext("test remote");
                subscriber.onCompleted();
            }
        }).delay(3, TimeUnit.SECONDS);
    }

}
