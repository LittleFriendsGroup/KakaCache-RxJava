package com.im4j.kakacache.rxjava.demo;

import com.im4j.kakacache.rxjava.CACHE;
import com.im4j.kakacache.rxjava.netcache.ResultData;
import com.im4j.kakacache.rxjava.netcache.strategy.CacheAndRemoteStrategy;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @version alafighting 2016-07
 */
public interface GitHubService {

    @GET("users/{user}/repos")
    rx.Observable<List<GithubRepoEntity>> listReposForNormal(@Path("user") String user);

    @GET("users/{user}/repos")
    @CACHE(value = "custom_key_listRepos", strategy = CacheAndRemoteStrategy.class)
    rx.Observable<ResultData<List<GithubRepoEntity>>> listReposForKaka(@Path("user") String user);

}
