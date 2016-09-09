package com.workable.errorhandler.matchers.retrofit;

import okhttp3.*;
import retrofit2.Response;

public class RetrofitHelper {

    private RetrofitHelper () {
    }

    public static okhttp3.Response generateMockResponseWith(int networkCode) {
        okhttp3.Response response = new okhttp3.Response.Builder()
                .code(networkCode)
                .message("OK")
                .body(convertStringResponseBody("MOCK"))
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build();

        return response;
    }

    public static Response generateSuccessResponseWith(int networkCode) {
        return Response.success(null, generateMockResponseWith(networkCode));
    }

    public static Response generateErrorResponseWith(int networkCode) {
        return Response.error(networkCode, generateMockResponseWith(networkCode).body());
    }

    public static ResponseBody convertStringResponseBody(String value) {
        return ResponseBody.create(MediaType.parse("text/plain"), value);
    }
}
