# java-error-handler
A tool for efficiently handling errors in your Java projects

## About
A common problem specially in software, and especially in UI software is that of handling errors.

Some errors, like network or some database errors designate truly exceptional conditions, while others are more to be expected like let's say input validation errors or authentication errors.

Some errors are common, cross-cut concerns, as they can happen throughout the app while others are specific to a single operation, screen or action.

Finally, there's always the possibility of dealing with a completely unknown error.

With that in mind, we usually want to:

* handle all common errors centrally from one place inside the codebase
* handle unknown errors is some other way, also from one place
* handle specific and expected errors depending on when and where they happen (i.e. on which action and on what screen)
* be able to override the rules above when needed (i.e. handle all network errors the same way, except for when sending an e-mmail)

_This tool allows you to do so efficiently while keeping your code DRY_


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
