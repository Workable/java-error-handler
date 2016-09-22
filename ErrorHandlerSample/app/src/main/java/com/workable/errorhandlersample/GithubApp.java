package com.workable.errorhandlersample;

import android.app.Application;
import android.util.Log;
import android.util.Range;

import com.workable.errorhandler.Action;
import com.workable.errorhandler.ErrorHandler;
import com.workable.errorhandler.Matcher;
import com.workable.errorhandler.MatcherFactory;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.adapter.rxjava.HttpException;


public class GithubApp extends Application {

    private static final String TAG = GithubApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        initErrorHandler();
    }

    private void initErrorHandler() {
        ErrorHandler
                .defaultErrorHandler()
                .bindErrorCodeClass(Integer.class, new MatcherFactory<Integer>() {
                    @Override
                    public Matcher build(Integer errorCode) {
                        return buildRetrofitMatcher(errorCode);
                    }
                })
                .bindErrorCode("offline", new MatcherFactory<String>() {
                    @Override
                    public Matcher build(String errorCode) {
                        return buildOfflineMatcher();
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
                if(throwable instanceof HttpException) {
                    HttpException httpException = (HttpException) throwable;
                    return httpException.code() == code;
                }
                return false;
            }
        };
    }

    private Matcher buildOfflineMatcher() {
        return new Matcher() {
            @Override
            public boolean matches(Throwable throwable) {
                return throwable instanceof UnknownHostException
                        || throwable instanceof ConnectException;
            }
        };
    }

}
