package com.im4j.kakacache.rxjava.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.im4j.kakacache.rxjava.netcache.RxRemoteCache;
import com.im4j.kakacache.rxjava.netcache.strategy.FirstTimeStrategy;

import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Demo主界面
 * @version alafighting 2016-06
 */
public class MainActivity extends AppCompatActivity {
    static final String KEY_CACHE = "key_cache";

    private Button btnTest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiity_main);

        btnTest = (Button) findViewById(R.id.btn_test_cache);
        btnTest.setOnClickListener(view -> {
            remote().compose(RxRemoteCache.transformer(KEY_CACHE, new FirstTimeStrategy())).subscribe(data -> {
                Log.e("main", "next  data=" + data);
            }, error -> {
                Log.e("main", "error", error);
            }, () -> {
                Log.e("main", "completed");
            });
        });
    }

    private rx.Observable<String> remote() {
        Log.e("main", "run remote1");
        return rx.Observable.just("test remote").delay(3, TimeUnit.SECONDS);
    }

}
