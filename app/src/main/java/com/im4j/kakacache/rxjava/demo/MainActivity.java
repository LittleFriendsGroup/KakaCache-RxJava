package com.im4j.kakacache.rxjava.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.im4j.kakacache.rxjava.KakaCache;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.netcache.retrofit.KakaRxCallAdapterFactory;
import com.im4j.kakacache.rxjava.netcache.strategy.FirstCacheStrategy;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

/**
 * Demo主界面
 * @version alafighting 2016-06
 */
public class MainActivity extends AppCompatActivity {
    static final String KEY_CACHE = "key_cache_listRepos";

    private Retrofit retrofit;
    private GitHubService service;

    private Button btnTestCache;
    private Button btnTestRetrofit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiity_main);

        LogUtils.DEBUG = false;
        KakaCache.init(this);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create(KakaCache.gson().create()))
                .addCallAdapterFactory(KakaRxCallAdapterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);

        btnTestCache = (Button) findViewById(R.id.btn_test_cache);
        btnTestCache.setOnClickListener(view -> {
            // 不修改原有代码，增加对Cache的支持
            service.listReposForNormal("alafighting")
                    .compose(KakaCache.transformer(KEY_CACHE, new FirstCacheStrategy()))
                    .subscribe(data -> {
                LogUtils.log("next  data=" + data);
            }, error -> {
                LogUtils.log("error", error);
            }, () -> {
                LogUtils.log("completed");
            });
        });

        btnTestRetrofit = (Button) findViewById(R.id.btn_test_retrofit);
        btnTestRetrofit.setOnClickListener(view -> {
            // 通过注解，自动支持Cache
            service.listReposForKaka("alafighting")
                    .subscribeOn(Schedulers.io())
                    .subscribe(data -> {
                        LogUtils.log("listReposForKaka => "+data);
                    }, error -> {
                        LogUtils.log(error);
                    });
        });
    }

}
