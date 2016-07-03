package com.im4j.kakacache.rxjava.demo;

import com.im4j.kakacache.rxjava.netcache.CACHE;
import com.im4j.kakacache.rxjava.netcache.ResultData;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @version alafighting 2016-07
 */
public interface GitHubService {

    @GET("users/{user}/repos")
    @CACHE("cache_key_listRepos")
    rx.Observable<ResultData<List<GithubRepoEntity>>> listRepos(@Path("user") String user,
                                        @Query("q") String q);

}
