package com.workable.errorhandlersample.retrofit;


import com.workable.errorhandlersample.models.GithubUser;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface GithubService {

    @GET(Routes.GITHUB_USERS + "{username}")
    Observable<GithubUser> searchUser(@Path("username") String username);

}
