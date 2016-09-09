package com.workable.errorhandler.matchers.retrofit;


import com.workable.errorhandler.Action;
import com.workable.errorhandler.ErrorHandler;
import junit.framework.TestCase;
import org.junit.Test;
import retrofit2.adapter.rxjava.HttpException;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class RetrofitMatcherFactoryTest extends TestCase {

    interface ActionDelegate {
        void action1();
    }

    private ActionDelegate actionDelegateMock;

    protected void setUp() throws Exception {
        actionDelegateMock = mock(ActionDelegate.class);
    }

    @Test
    public void test_catching_exact_http_code() {
        ErrorHandler
                .createIsolated()
                .bindErrorCode(400, RetrofitMatcherFactory.create())
                .bindErrorCodeClass(Range.class, RetrofitMatcherFactory.createRange())
                .on(400, new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        actionDelegateMock.action1();
                    }
                })
                .on(Range.of(400, 500), new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        actionDelegateMock.action1();
                    }
                })
                .handle(new HttpException(RetrofitHelper.generateErrorResponseWith(400)));


        Mockito.verify(actionDelegateMock, times(2)).action1();
    }

    @Test
    public void test_not_catching_exact_http_code() {
        ErrorHandler
                .createIsolated()
                .bindErrorCode(400, RetrofitMatcherFactory.create())
                .bindErrorCodeClass(Range.class, RetrofitMatcherFactory.createRange())
                .on(400, new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        actionDelegateMock.action1();
                    }
                })
                .on(Range.of(450, 450), new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        actionDelegateMock.action1();
                    }
                })
                .handle(new HttpException(RetrofitHelper.generateErrorResponseWith(401)));


        Mockito.verify(actionDelegateMock, times(0)).action1();
    }

    @Test
    public void test_catching_with_class() {
        ErrorHandler
                .createIsolated()
                .bindErrorCodeClass(Integer.class, RetrofitMatcherFactory.create())
                .on(500, new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        actionDelegateMock.action1();
                    }
                })
                .handle(new HttpException(RetrofitHelper.generateErrorResponseWith(401)));


        Mockito.verify(actionDelegateMock, times(0)).action1();
    }
}
