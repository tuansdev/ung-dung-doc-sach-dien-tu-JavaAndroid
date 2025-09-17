package com.example.ebookreader.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GutendexApi {
    @GET("books")
    Call<GutendexResponse> getBooks(
            @Query("page") int page,
            @Query("search") String search,
            @Query("topic") String topic,
            @Query("author") String author

    );
}