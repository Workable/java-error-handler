package com.workable.errorhandler;

import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

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

        void action6();

        void action7();

        void action8();

        void action9();

        void action10();

        void otherwise1();

        void otherwise2();

        void always1();

        void always2();
    }

    ActionDelegate actionDelegateMock = mock(ActionDelegate.class);

    protected void setUp() {
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
            .on(FooException.class, (throwable, errorHandler) -> actionDelegateMock.action9())
            .on(500, (throwable, errorHandler) -> actionDelegateMock.action10())
            .otherwise((throwable, errorHandler) -> actionDelegateMock.otherwise2())
            .always((throwable, errorHandler) -> actionDelegateMock.always2());
    }

    protected void tearDown() {
        ErrorHandler
            .defaultErrorHandler()
            .clear();
    }

    @Test
    public void testActionsExecutionOrder() {
        ErrorHandler errorHandler1 = ErrorHandler
            .create()
            .on(FooException.class, (throwable, errorHandler) -> actionDelegateMock.action1())
            .on(
                (throwable) -> {
                    try {
                        return FooException.class.cast(throwable).isFatal();
                    } catch (ClassCastException ignore) {
                        return false;
                    }
                },
                (throwable, errorHandler) -> actionDelegateMock.action2()
            )
            .on("closed:bar", (throwable, errorHandler) -> actionDelegateMock.action3())
            .on(400, (throwable, errorHandler) -> actionDelegateMock.action4())
            .on(500, (throwable, errorHandler) -> actionDelegateMock.action5())
            .otherwise((throwable, errorHandler) -> actionDelegateMock.otherwise1())
            .always((throwable, errorHandler) -> actionDelegateMock.always1());


        InOrder testVerifier1 = inOrder(actionDelegateMock);

        errorHandler1.handle(new FooException("test1"));

        testVerifier1.verify(actionDelegateMock).action1();
        testVerifier1.verify(actionDelegateMock).always1();
        testVerifier1.verify(actionDelegateMock).action9();
        testVerifier1.verify(actionDelegateMock).always2();
        testVerifier1.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(actionDelegateMock);

        reset(actionDelegateMock);

        InOrder testVerifier2 = inOrder(actionDelegateMock);

        errorHandler1.handle(new BarException("What a shame", false));

        testVerifier2.verify(actionDelegateMock).action3();
        testVerifier2.verify(actionDelegateMock).always1();
        testVerifier2.verify(actionDelegateMock).always2();
        testVerifier2.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(actionDelegateMock);

        reset(actionDelegateMock);


        InOrder testVerifier3 = inOrder(actionDelegateMock);

        errorHandler1.handle(new QuxException(500));

        testVerifier3.verify(actionDelegateMock).action5();
        testVerifier3.verify(actionDelegateMock).always1();
        testVerifier3.verify(actionDelegateMock).action10();
        testVerifier3.verify(actionDelegateMock).always2();
        testVerifier3.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(actionDelegateMock);
    }

}
