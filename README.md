# ErrorHandler

[![Travis](https://img.shields.io/travis/workable/java_error_handler.svg)]() [![Bintray](https://img.shields.io/bintray/v/workable/maven/java_error_handler.svg?maxAge=2592000)]()

> Error handling library for Android and Java

## Download
Download the  or grab via Maven:
```xml
<dependency>
  <groupId>com.workable</groupId>
  <artifactId>error-handler</artifactId>
  <version>0.9</version>
  <type>pom</type>
</dependency>
```
or Gradle:
```groovy
compile 'com.workable:error-handler:0.9'
```

## Usage

## Example
Let's say we're building a messaging app for Android that uses Foo service for crash reporting.

*WIP*

```java
ErrorHandler
  .defaultErrorHandler()
  // bind an error matcher to a code
  .bindErrorCode("closed:bar", errorCode -> throwable -> {
      if (throwable instanceof BarException) {
          return !((BarException) throwable).isOpenBar();
      } else {
          return false;
      }
  })
  // bind an error matcher to a code class, for example integers could designate HTTP errors
  .bindErrorCodeClass(Integer.class, errorCode -> throwable -> {
      if (throwable instanceof HttpException) {
          return ((HttpException) throwable).getHttpStatus() == errorCode;
      } else {
          return false;
      }
  })
  // now let's handle all general, cross-cut or uncommon errors here
  .on(FooException.class, (throwable, errorHandler) -> {
    // handle foo errors
  })
  .on(
    (throwable) -> {
        try {
            return NetworkException.class.cast(throwable).isOffline();
        } catch (ClassCastException ignore) {
            return false;
        }
    },
    (throwable, errorHandler) -> {
      // handle offline here  
    }
  )
  .on(500, (throwable, errorHandler) -> {
    // handle HTTP 500 errors
  })
  .otherwise((throwable, errorHandler) -> {
    // handle unknown errors
  })
  .always((throwable, errorHandler) -> {
    // log the error
  });
```

Then on a specific part of your app, most probably an action handler inside a sceen, controller etc.

```java
 ErrorHandler
    .create()
    .on(FooException.class, (throwable, errorHandler) -> {
      // handle foo here and don't let the default handler deal with it
      errorHandler.skipDefaults();
    })
    .on("closed:bar", (throwable, errorHandler) -> {
      // handle an error by it's code
    })
    .on(422, (throwable, errorHandler) -> {
      // handle the specific validation error at hand
    });
```

## API

> Create a new Instance of ErrorHandler with no default ErrorHandler.

```java
createIsolated()
```

> Create a new Instance of ErrorHandler that will use the default ErrorHandler (if set).

```java
create()
```

> Get the default ErrorHandler instance. If there is not one available, it creates a new Instance.
> *Warning:* This method is synchronized, so be careful when you try to retrieve it from multiple Threads.

```java
defaultErrorHandler()
```

> _Action_ will be executed if a given Throwable matches with _Matcher_.

```java
on(Matcher, Action)
```

> _Action_ will be executed if a given Throwable is an instance of _<? extends Exception>_.

```java
on(Class<? extends Exception>, Action)
```

> _Action_ will be executed if a given Throwable is bound to T, through `bindErrorCode()`.

```java
on(T, Action)
```

> _Action_ will be executed if no previous _Action_ has been executed.

```java
otherwise(Action)
```

> _Action_ will be executed for every given Throwable.

```java
always(Action)
```

> Instruct the current ErrorHandler instance to skip the execution of subsequent registered _Actions_, through `on()`.

```java
skipFollowing()
```

> Instruct the current ErrorHandler instance to skip the execution of subsequent registered _Actions_ through `always()`.

```java
skipAlways()
```

> Instruct the current ErrorHandler instance to skip the execution of registered _Actions_ on the _defaultErrorHandler_ instance.

```java
skipDefaults()
```

> The _Throwable_ that we should act upon.

```java
handle(Throwable)
```

> Bind a _MatcherFactory_ to _T_.

```java
bindErrorCode(T, MatcherFactory)
```

> Bind a _MatcherFactory_ to _Class<T>_.

```java
bindErrorCodeClass(Class<T>, MatcherFactory)
```

> Clear current ErrorHandler instance from all _Actions_ and _Matchers_.

```java
clear()
```


## About
A common problem in software, specially in UI software is that of error handling.

One way to classify errors can be by how they relate to the [problem domain](https://en.wikipedia.org/wiki/Problem_domain). Some errors, like `network` or `database` errors are orthogonal to the problem domain and designate truly **exceptional conditions**, while others are core parts of the domain like `validation` and `authentication` errors.

Another way to classify errors is by their **scope**. Are they **common** throughout the application or **specific** to a single screen, object or even method? Think of `UnauthorizedException` versus `InvalidPasswordException`.

And let's not forget another very simple distintion between errors. Those that are known at authoring time and thus **expected** (despite of how probable is that they occur), and those that are **unknown** until runtime.

With that in mind, we usually want to:

1. have a **default** handler for every **expected** (exceptional, common or not) error
2. handle **specific** errors **as appropriate** based on where and when they occur
3. have a **default** catch-all handler for **unknown** errors
4. **override** any default handler if needed
5. keep our code **DRY**

Java, as a language, provides you with a way to do the above. By mapping exceptional or very common errors to runtime exceptions and catching them lower in the call stack, while having specific expected errors mapped to checked exceptions and handle them near where the error occurred. Still, countless are the projects where this simple strategy has gone astray with lots of errors being either swallowed or left for the catch-all `Thread.UncaughtExceptionHandler`. Moreover, it usually comes with significant boilerplate code. `ErrorHandler` however eases this practice through its fluent API, error aliases and defaults mechanism.

This library doesn't try to solve Java specific problems, although it does help with the `log and shallow` anti-pattern as it provides an opinionated and straightforward way to act inside every `catch` block.  It was created for the needs of an Android app and proved itself useful very quickly. So it may work for you as well. If you like the concept and you're developing in  _Swift_ or _Javascript_, we're baking em and will be available really soon.

## License

The MIT License

Copyright (c) 2010-2016 Workable SA

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
