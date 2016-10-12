# ChangeLog

## v1.0.0

### New

 - `errorHandler.run(BlockExecutor)` saves you from a `try/catch` block or two 
   ```java
     try {
        doSomething();
     } catch(Exception ex) {
        errorHandler.handle(ex);
     }
     
     // can now be written as
     
     errorHandler.run(() -> doSomething())
   ```

### Breaking
 
 - `bindErrorCode` renamed to `bind`
 - `bindErrorCodeClass` renamed to `bindClass`
 




