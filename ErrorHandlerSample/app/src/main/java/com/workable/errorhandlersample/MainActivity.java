package com.workable.errorhandlersample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.workable.errorhandler.Action;
import com.workable.errorhandler.ErrorHandler;
import com.workable.errorhandler.Matcher;
import com.workable.errorhandler.MatcherFactory;
import com.workable.errorhandlersample.models.GithubUser;
import com.workable.errorhandlersample.retrofit.GithubService;
import com.workable.errorhandlersample.retrofit.Routes;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String USERNAME = "charbgr";

    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit.Builder builder = new Retrofit.Builder()
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .baseUrl(Routes.GITHUB_API_BASE);

        retrofit = builder.build();

        buildErrorHandler();
        initiateAPICall();
    }

    private void initiateAPICall() {
        retrofit.create(GithubService.class).searchUser(USERNAME)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GithubUser>() {
                    @Override
                    public void onCompleted() {
                        //nothing here
                    }

                    @Override
                    public void onError(Throwable e) {
                        handleWithErrorHandler(e);
                        handleWithoutErrorHandler(e);
                    }

                    @Override
                    public void onNext(GithubUser githubUser) {
                        System.out.println(githubUser);
                    }
                });
    }

    private void handleWithErrorHandler(Throwable e) {
        ErrorHandler
                .create()
                .on(404, new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        //We prefer to handle 404 specifically on this API call

                        /*
                        * We would also like to skip the execution of default ErrorHandler's
                        * on(404, Action) method
                        * */
                        errorHandler.skipDefaults();

                        /*
                        * We would also like to skip the execution of default ErrorHandler's
                        * always(Action) method
                        * */
                        errorHandler.skipAlways();

                    }
                })
                .handle(e);
    }

    private void handleWithoutErrorHandler(Throwable e) {
        /*
        * All handling code of the Throwable here would be probably duplicated
        * in more places throughout our code, for other API calls.
        * */
        HttpException httpException = (HttpException) e;

        if (httpException.code() == 404) {
            //Do something for 404
        } else if (httpException.code() == 500) {
            //Do something for 500
        }
    }

    private void buildErrorHandler() {
        ErrorHandler
                .defaultErrorHandler()
                .bindErrorCode(404, new MatcherFactory<Integer>() {
                    @Override
                    public Matcher build(Integer errorCode) {
                        return buildRetrofitMatcher(errorCode);
                    }
                })
                .on(404, new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        //Do something with our error here
                    }
                })
                .always(new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        Log.e(TAG, "We are logging an error here", throwable);
                    }
                });
    }

    private Matcher buildRetrofitMatcher(final int code) {
        return new Matcher() {
            @Override
            public boolean matches(Throwable throwable) {
                HttpException httpException = (HttpException) throwable;
                return httpException.code() == code;
            }
        };
    }
}
