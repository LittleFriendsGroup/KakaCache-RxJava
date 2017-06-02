package com.im4j.kakacache.rxjava.demo;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @version alafighting 2016-07
 */
public interface GitHubService {

    @GET("users/{user}/repos")
    Observable<List<GithubRepoEntity>> listReposForNormal(@Path("user") String user);

}
