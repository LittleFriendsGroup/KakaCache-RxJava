package com.im4j.kakacache.rxjava.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.netcache.KakaCache;
import com.im4j.kakacache.rxjava.netcache.KakaRetrofit;
import com.im4j.kakacache.rxjava.netcache.strategy.CacheAndRemoteStrategy;
import com.im4j.kakacache.rxjava.netcache.strategy.FirstCacheStrategy;

import java.util.concurrent.TimeUnit;

import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
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
            KakaRetrofit retrofit = new KakaRetrofit.Builder()
                    .baseUrl("https://api.github.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            GitHubService service = retrofit.create(GitHubService.class);
            service.listRepos("octocat").compose(KakaCache.transformer(KEY_CACHE, new FirstCacheStrategy())).subscribeOn(Schedulers.io()).subscribe(data -> {
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
