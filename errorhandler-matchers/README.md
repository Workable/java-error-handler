# ErrorHandler Matchers

By default ErrorHandler provides you with a public, fully customizable MatcherFactory interface. The child modules contained in this folder are officially supported matchers for well known Networking libraries.

In order to use them, provide an instance of your desired MatcherFactory when building your ErrorHandler instances.

```java

ErrorHandler
  .create()
  .bind(400, RetrofitMatcherFactory.create())
  .on(400, (throwable, errorHandler) -> showErrorMessage("what?"))
  .handle(httpException);

// Or bind all integers to Retrofit errors

ErrorHandler
  .create()
  .bindClass(Range.class, RetrofitMatcherFactory.createRange())
  .bindClass(Integer.class, RetrofitMatcherFactory.create())
  .on(400, (throwable, errorHandler) -> showErrorMessage("what?"))
  .on(Range.of(500, 599), (throwable, errorHandler) -> showErrorMessage("kaboom"))
  .handle(httpException);

```
