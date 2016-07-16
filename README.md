# java-error-handler
A tool for efficiently handling errors in your Java projects

## About
A common problem in software, specially in UI software is that of error handling.

Some errors, like `network` or `database` errors designate truly **exceptional conditions**, while others are core parts of the business logic flow like let's say input `validation` or `authentication` errors.

Some errors are common, **cross-cut concerns** as they can occur throughout the app while others are **specific** to a single operation, screen or action. Think of `UnauthorizedException` versus `InvalidPasswordException`.

Finally, there's always the possibility of dealing with a completely unknown, at authoring/compile time, error.

With that in mind, we usually want to:

1. have a default handler for every expected error
2. have a default handler for every exceptional or cross-cut concern error
3. handle specific errors depending on when and where they occur
4. override defaults when needed
5. keep our code DRY 

Java, as a language, provides you with a way to do the above. By mapping exceptional cross-cut errors to runtime exceptions and catching them lower in the call stack, while having specific expected errors mapped to checked exceptions and handle them near where the error occurred. Still, countless are the projects where this simple strategy has gone astray with lots of errors being either swallowed or left for the `Thread.UncaughtExceptionHandler`. Moreover, it usually comes with significant boilerplate code.

> `ErrorHandler` was built to make handling and logging every error easier, with no boilerplate and less room for mistakes. 

This library doesn't solve Java specific error handling problems, although it does help with the `log and shallow` anti-pattern as it provides an opinionated and straightforward way to act inside every `catch` clause.  It was created for the needs of an Android app and it proved itself useful very quickly. So it may work for you as well. If you like the concept and you're developing in  _Swift_ or _Javascript_, we're baking em and will be available really soon.

## Usage
Setup a default error handler

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
