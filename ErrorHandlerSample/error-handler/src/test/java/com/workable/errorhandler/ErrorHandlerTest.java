package com.workable.errorhandler;

import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

/**
 * {@link ErrorHandler} unit tests
 *
 * @author Stratos Pavlakis
 */
public class ErrorHandlerTest extends TestCase {

    interface ActionDelegate {

        void action1();

        void action2();

        void action3();

        void action4();

        void action5();

        void otherwise1();

        void always1();

        void defaultAction1();

        void defaultAction2();

        void defaultOtherwise();

        void defaultAlways();
    }

    private ActionDelegate actionDelegateMock;

    protected void setUp() {
        actionDelegateMock = mock(ActionDelegate.class);

        ErrorHandler
            .defaultErrorHandler()
            .bindErrorCode("closed:bar", errorCode -> throwable -> {
                if (throwable instanceof BarException) {
                    return !((BarException) throwable).isOpenBar();
                } else {
                    return false;
                }
            })
            .bindErrorCodeClass(Integer.class, errorCode -> throwable -> {
                if (throwable instanceof QuxException) {
                    return ((QuxException) throwable).getErrorStatus() == errorCode;
                } else {
                    return false;
                }
            })
            .on(FooException.class, (throwable, errorHandler) -> actionDelegateMock.defaultAction1())
            .on(500, (throwable, errorHandler) -> actionDelegateMock.defaultAction2())
            .otherwise((throwable, errorHandler) -> actionDelegateMock.defaultOtherwise())
            .always((throwable, errorHandler) -> actionDelegateMock.defaultAlways());
    }

    protected void tearDown() {
        ErrorHandler
            .defaultErrorHandler()
            .clear();
    }

    @Test
    public void testActionsExecutionOrder() {
        ErrorHandler errorHandler = ErrorHandler
            .create()
            .on(FooException.class, (throwable, handler) -> actionDelegateMock.action1())
            .on(
                (throwable) -> {
                    try {
                        return FooException.class.cast(throwable).isFatal();
                    } catch (ClassCastException ignore) {
                        return false;
                    }
                },
                (throwable, handler) -> actionDelegateMock.action2()
            )
            .on("closed:bar", (throwable, handler) -> actionDelegateMock.action3())
            .on(400, (throwable, handler) -> actionDelegateMock.action4())
            .on(500, (throwable, handler) -> actionDelegateMock.action5())
            .otherwise((throwable, handler) -> actionDelegateMock.otherwise1())
            .always((throwable, handler) -> actionDelegateMock.always1());


        InOrder testVerifier1 = inOrder(actionDelegateMock);

        errorHandler.handle(new FooException("test1"));

        testVerifier1.verify(actionDelegateMock).action1();
        testVerifier1.verify(actionDelegateMock).always1();
        testVerifier1.verify(actionDelegateMock).defaultAction1();
        testVerifier1.verify(actionDelegateMock).defaultAlways();
        testVerifier1.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(actionDelegateMock);

        reset(actionDelegateMock);

        InOrder testVerifier2 = inOrder(actionDelegateMock);

        errorHandler.handle(new BarException("What a shame", false));

        testVerifier2.verify(actionDelegateMock).action3();
        testVerifier2.verify(actionDelegateMock).always1();
        testVerifier2.verify(actionDelegateMock).defaultAlways();
        testVerifier2.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(actionDelegateMock);

        reset(actionDelegateMock);


        InOrder testVerifier3 = inOrder(actionDelegateMock);

        errorHandler.handle(new QuxException(500));

        testVerifier3.verify(actionDelegateMock).action5();
        testVerifier3.verify(actionDelegateMock).always1();
        testVerifier3.verify(actionDelegateMock).defaultAction2();
        testVerifier3.verify(actionDelegateMock).defaultAlways();
        testVerifier3.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(actionDelegateMock);
    }

    @Test
    public void testSkipDefaults() {
        ErrorHandler
            .create()
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action1();
            })
            .handle(new FooException("foo error"));

        Mockito.verify(actionDelegateMock, times(1)).action1();
        Mockito.verify(actionDelegateMock, times(1)).defaultAction1();

        reset(actionDelegateMock);

        ErrorHandler
            .create()
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action1();
                handler.skipDefaults();
            })
            .handle(new FooException("foo error"));

        Mockito.verify(actionDelegateMock, times(1)).action1();
        Mockito.verify(actionDelegateMock, never()).defaultAction1();
    }

    @Test
    public void testSkipFollowing() {
        InOrder testVerifier = inOrder(actionDelegateMock);

        ErrorHandler
            .create()
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action1();
            })
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action2();
                handler.skipFollowing();
            })
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action3();
            })
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action4();
            })
            .handle(new FooException("foo error"));

        testVerifier.verify(actionDelegateMock).action1();
        testVerifier.verify(actionDelegateMock).action2();
        testVerifier.verify(actionDelegateMock).defaultAlways();
        testVerifier.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(actionDelegateMock);
    }

    @Test
    public void testSkipAlways() {
        InOrder testVerifier = inOrder(actionDelegateMock);

        ErrorHandler
            .create()
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action1();
            })
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action2();
                handler.skipAlways();
            })
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action3();
            })
            .on(FooException.class, (throwable, handler) -> {
                actionDelegateMock.action4();
            })
            .handle(new FooException("foo error"));

        testVerifier.verify(actionDelegateMock).action1();
        testVerifier.verify(actionDelegateMock).action2();
        testVerifier.verify(actionDelegateMock).action3();
        testVerifier.verify(actionDelegateMock).action4();
        testVerifier.verify(actionDelegateMock).defaultAction1();
        testVerifier.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(actionDelegateMock);
    }

}
