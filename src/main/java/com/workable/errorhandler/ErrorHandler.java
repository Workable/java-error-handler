/*
 * The MIT License
 *
 * Copyright (c) 2010-2016 Workable SA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.workable.errorhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An ErrorHandler is responsible for handling an error by executing one or more actions,
 * instances of {@link Action}, that are found to match the error.
 *
 * @author Stratos Pavlakis <pavlakis@workable.com>
 * @author Pavlos-Petros Tournaris <tournaris@workable.com>
 * @author Vasilis Charalampakis <basilis@workable.com>
 */
public class ErrorHandler {

    private static ErrorHandler defaultInstance = null;

    private Map<ErrorCodeIdentifier, MatcherFactory> errorCodeMap;

    private List<ActionEntry> actions;
    private List<Action> otherwiseActions;
    private List<Action> alwaysActions;

    private ThreadLocal<Context> localContext;

    private ErrorHandler parentErrorHandler;

    /**
     * Need a private constructor as we want new instances created
     * only via the {@link #create} methods.
     */
    private ErrorHandler() {
        super();
        this.actions = new ArrayList<>();
        this.otherwiseActions = new ArrayList<>();
        this.alwaysActions = new ArrayList<>();
        this.errorCodeMap = new HashMap<>();
    }

    /**
     * Create a new ErrorHandler with the given one as parent.
     *
     * @param parentErrorHandler the parent @{link ErrorHandler}
     */
    private ErrorHandler(ErrorHandler parentErrorHandler) {
        this();
        this.parentErrorHandler = parentErrorHandler;
    }

    /**
     * Create a new @{link ErrorHandler}, isolated from the default one.
     * <p>
     * In other words, designed to handle all errors by itself without delegating
     * to the default error handler.
     *
     * @return returns a new instance of ErrorHandler
     */
    public static ErrorHandler createIsolated() {
        return new ErrorHandler();
    }

    /**
     * Create a new @{link ErrorHandler}, that delegates to the default one.
     * <p>
     * Any default actions, are always executed after the ones registered on this one.
     *
     * @return returns a new instance of ErrorHandler
     */
    public static ErrorHandler create() {
        return new ErrorHandler(defaultErrorHandler());
    }

    /**
     * Get the default @{link ErrorHandler}, a singleton object
     * to which all other instances by default delegate to.
     *
     * @return the default @{link ErrorHandler} instance
     */
    public static synchronized ErrorHandler defaultErrorHandler() {
        if (defaultInstance == null) {
            defaultInstance = new ErrorHandler();
        }
        return defaultInstance;
    }

    /**
     * Register {@param action} to be executed by {@link #handle(Throwable)},
     * if the thrown error matches the {@param matcher}.
     *
     * @param matcher a matcher to match the thrown error
     * @param action  the associated action
     * @return the current instance of ErrorHandler
     */
    public ErrorHandler on(Matcher matcher, Action action) {
        if (matcher == null) {
            throw new IllegalArgumentException("matcher cannot be null");
        }
        assertNotNullAction(action);
        this.actions.add(ActionEntry.from(matcher, action));
        return this;
    }

    /**
     * Register {@param action} to be executed by {@link #handle(Throwable)},
     * if the thrown error is an instance of {@param exceptionClass}.
     *
     * @param exceptionClass the class of the error
     * @param action         the associated action
     * @return the current instance of ErrorHandler
     */
    public ErrorHandler on(Class<? extends Exception> exceptionClass, Action action) {
        if (exceptionClass == null) {
            throw new IllegalArgumentException("exceptionClass cannot be null");
        }
        assertNotNullAction(action);
        actions.add(ActionEntry.from(new ExceptionMatcher(exceptionClass), action));
        return this;
    }

    /**
     * Register {@param action} to be executed by {@link #handle(Throwable)},
     * if the thrown error is bound (associated) to {@param errorCode}.
     * <p/>
     * See {@link #bindErrorCodeClass(Class, MatcherFactory)} and {@link #bindErrorCode(Object, MatcherFactory)}
     * on how to associate arbitrary error codes with actual Throwables via {@link Matcher}.
     *
     * @param errorCode the error code
     * @param action    the associated action
     * @return the current instance of ErrorHandler
     */
    public <T> ErrorHandler on(T errorCode, Action action) {
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode cannot be null");
        }

        MatcherFactory<? super T> matcherFactory = getMatcherFactoryForErrorCode(errorCode);
        if (matcherFactory == null) {
            throw new UnknownErrorCodeException(errorCode);
        }

        actions.add(ActionEntry.from(matcherFactory.build(errorCode), action));
        return this;
    }

    /**
     * Register {@param action} to be executed in case no other <em>conditional</em>
     * action gets executed.
     *
     * @param action the action
     * @return the current instance of ErrorHandler
     */
    public ErrorHandler otherwise(Action action) {
        assertNotNullAction(action);
        otherwiseActions.add(action);
        return this;
    }

    /**
     * Register {@param action} to be executed on all errors.
     *
     * @param action the action
     * @return the current instance of ErrorHandler
     */
    public ErrorHandler always(Action action) {
        assertNotNullAction(action);
        alwaysActions.add(action);
        return this;
    }

    /**
     * Skip all following actions registered via an {@code on} method
     */
    public ErrorHandler skipFollowing() {
        if (localContext != null) {
            localContext.get().skipFollowing = true;
        }
        return this;
    }

    /**
     * Skip all actions registered via {@link #always(Action)}
     */
    public ErrorHandler skipAlways() {
        if (localContext != null) {
            localContext.get().skipAlways = true;
        }
        return this;
    }

    /**
     * Skip the default matching actions if any
     */
    public ErrorHandler skipDefaults() {
        if (localContext != null) {
            localContext.get().skipDefaults = true;
        }
        return this;
    }

    protected void handle(Throwable error, ThreadLocal<Context> context) {
        if (error == null)
            throw new IllegalArgumentException("error to be checked can not be null");

        localContext = context;

        Context ctx = localContext.get();

        for (ActionEntry actionEntry : actions) {
            if (ctx.skipFollowing) break;
            if (actionEntry.matcher.matches(error)) {
                actionEntry.action.execute(error, this);
                ctx.handled = true;
            }
        }

        if (!ctx.handled && !otherwiseActions.isEmpty()) {
            for (Action action : otherwiseActions) {
                action.execute(error, this);
                ctx.handled = true;
            }
        }

        if (!ctx.skipAlways) {
            for (Action action : alwaysActions) {
                action.execute(error, this);
                ctx.handled = true;
            }
        }

        if (parentErrorHandler != null && !ctx.skipDefaults) {
            parentErrorHandler.handle(error, localContext);
        }
    }


    /**
     * Handle {@param error} by executing all matching actions.
     *
     * @param error the error as a {@link Throwable}
     */
    public void handle(Throwable error) {
        this.handle(error, new ThreadLocal<Context>() {
            @Override
            protected Context initialValue() {
                return new Context();
            }
        });
    }

    /**
     * Bind an ErrorCode {@param errorCode} with a Matcher,
     * through the provided MatcherFactory {@param matcherFactory}.
     * */
    public <T> ErrorHandler bindErrorCode(T errorCode, MatcherFactory<? super T> matcherFactory) {
        errorCodeMap.put(new ErrorCodeIdentifier<>(errorCode), matcherFactory);
        return this;
    }

    /**
     * Bind an ErrorCode Class {@param errorCodeClass} with a Matcher,
     * through the provided MatcherFactory {@param matcherFactory}.
     * */
    public <T> ErrorHandler bindErrorCodeClass(Class<T> errorCodeClass, MatcherFactory<? super T> matcherFactory) {
        errorCodeMap.put(new ErrorCodeIdentifier<>(errorCodeClass), matcherFactory);
        return this;
    }

    @SuppressWarnings("unchecked")
    protected <T> MatcherFactory<? super T> getMatcherFactoryForErrorCode(T errorCode) {
        MatcherFactory<T> matcherFactory;
        matcherFactory = errorCodeMap.get(new ErrorCodeIdentifier<>(errorCode));

        if (matcherFactory != null) {
            return matcherFactory;
        }

        matcherFactory = errorCodeMap.get(new ErrorCodeIdentifier(errorCode.getClass()));

        if (matcherFactory != null) {
            return matcherFactory;
        }

        if (parentErrorHandler != null) {
            return parentErrorHandler.getMatcherFactoryForErrorCode(errorCode);
        }

        return null;
    }

    /**
     * Clear ErrorHandler instance from all its registered Actions and Matchers.
     */
    public void clear() {
        actions.clear();
        errorCodeMap.clear();
        otherwiseActions.clear();
        alwaysActions.clear();
        if (localContext != null) {
            localContext.get().clear();
        }
    }

    /**
     * Throws if {@param action} is null
     *
     * @param action the action to assert against
     */
    private void assertNotNullAction(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("action cannot be null");
        }
    }

    private static class Context {
        private HashMap<String, Object> keys = new HashMap<>();

        boolean handled;
        boolean skipDefaults = false;
        boolean skipFollowing = false;
        boolean skipAlways = false;

        public Object get(Object key) {
            return keys.get(key);
        }

        public Object put(String key, Object value) {
            return keys.put(key, value);
        }

        public Object remove(Object key) {
            return keys.remove(key);
        }

        void clear() {
            keys.clear();
            skipDefaults = false;
            skipFollowing = false;
            skipAlways = false;
        }
    }

    /**
     * Used to identify an error code either by its "literal" value
     * or by its Class.
     * <p/>
     * When using custom objects as error codes,
     * make sure you implement {@link Object#equals(Object)} to allow ErrorHandler
     * perform equality comparisons between instances.
     */
    private static final class ErrorCodeIdentifier<T> {
        private T errorCode;
        private Class<T> errorCodeClass;

        ErrorCodeIdentifier(T errorCode) {
            this.errorCode = errorCode;
        }

        ErrorCodeIdentifier(Class<T> errorCodeClass) {
            this.errorCodeClass = errorCodeClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ErrorCodeIdentifier that = (ErrorCodeIdentifier) o;

            if (errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null)
                return false;
            return errorCodeClass != null ? errorCodeClass.equals(that.errorCodeClass) : that.errorCodeClass == null;

        }

        @Override
        public int hashCode() {
            int result = errorCode != null ? errorCode.hashCode() : 0;
            result = 31 * result + (errorCodeClass != null ? errorCodeClass.hashCode() : 0);
            return result;
        }
    }
}
