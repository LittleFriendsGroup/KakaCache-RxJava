package com.im4j.kakacache.rxjava.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.im4j.kakacache.rxjava.KakaCache;
import com.im4j.kakacache.rxjava.common.utils.L;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.netcache.ResultData;
import com.im4j.kakacache.rxjava.netcache.strategy.CacheStrategy;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Demo主界面
 * @version alafighting 2016-06
 */
public class MainActivity extends AppCompatActivity {
    static final String KEY_CACHE = "key_cache_listRepos";

    private Retrofit retrofit;
    private GitHubService service;

    private Button btnTestCache;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiity_main);

        L.isDebug = false;
        KakaCache.init(this, Utils.getUsableCacheDir(this));

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);

        btnTestCache = (Button) findViewById(R.id.btn_test_cache);
        btnTestCache.setOnClickListener(view -> {
            demoForNormal();
        });
    }

    /**
     * 案例一：不修改原有代码，增加对Cache的支持
     */
    void demoForNormal() {
        service.listReposForNormal("imkarl")
                .compose(KakaCache.transformer(KEY_CACHE, CacheStrategy.FirstCache))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    L.log("next  data=" + data);
                }, error -> {
                    L.log("error");
                    L.log(error);
                }, () -> {
                    L.log("completed");
                });
    }

}
