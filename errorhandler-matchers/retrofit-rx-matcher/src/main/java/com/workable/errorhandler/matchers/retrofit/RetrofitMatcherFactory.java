package com.workable.errorhandler.matchers.retrofit;

import com.workable.errorhandler.Matcher;
import com.workable.errorhandler.MatcherFactory;
import retrofit2.adapter.rxjava.HttpException;



public class RetrofitMatcherFactory {

    private RetrofitMatcherFactory() {
        // no instances
    }

    /**
     * Creates a {@link MatcherFactory} that checks HTTP statuses
     *
     * @return new MatcherFactory for Retrofit Rx HttpException that works with Integer
     */
    public static MatcherFactory<Integer> create() {
        return new MatcherFactory<Integer>() {
            public Matcher build(final Integer httpStatusCode) {
                return new Matcher() {
                    public boolean matches(Throwable throwable) {
                       return throwable instanceof HttpException &&
                               ((HttpException) throwable).code() == httpStatusCode;
                    }
                };
            }
        };
    }

    /**
     * Creates a {@link MatcherFactory} that checks if HTTP status is in given {@link Range}
     *
     * @return new MatcherFactory for Retrofit Rx HttpException that works with Range
     */
    public static MatcherFactory<Range> createRange() {
        return new MatcherFactory<Range>() {
            public Matcher build(final Range range) {
                return new Matcher() {
                    public boolean matches(Throwable throwable) {
                        return throwable instanceof HttpException &&
                                range.contains(((HttpException)throwable).code());
                    }
                };
            }
        };
    }
}
