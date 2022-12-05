package com.example.lesorac.network;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiServices {
    @POST("send")
    Call<String> sendMessages(
            @HeaderMap HashMap<String, String> headers,
            @Body String messageBody
    );
}
