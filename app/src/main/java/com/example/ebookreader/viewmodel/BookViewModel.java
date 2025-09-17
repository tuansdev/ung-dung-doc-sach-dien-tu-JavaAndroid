package com.example.ebookreader.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ebookreader.api.Book;
import com.example.ebookreader.api.GutendexApi;
import com.example.ebookreader.api.GutendexResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookViewModel extends ViewModel {
    private final GutendexApi api;

    public BookViewModel() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gutendex.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(new Interceptor() {
                            @Override
                            public okhttp3.Response intercept(Chain chain) throws IOException {
                                Request request = chain.request();
                                okhttp3.Response response = chain.proceed(request);
                                int tryCount = 0;
                                while (!response.isSuccessful() && tryCount < 3) {
                                    tryCount++;
                                    response.close();
                                    response = chain.proceed(request);
                                }
                                return response;
                            }
                        })
                        .build())
                .build();
        api = retrofit.create(GutendexApi.class);
    }

    public LiveData<List<Book>> getBooks(int page, String search, String topic) {
        MutableLiveData<List<Book>> books = new MutableLiveData<>();

        api.getBooks(page, search, topic, "").enqueue(new Callback<GutendexResponse>() {
            @Override
            public void onResponse(Call<GutendexResponse> call, Response<GutendexResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API", "Response: " + response.body().results.toString());
                    books.postValue(response.body().results);
                } else {
                    Log.e("API", "Response failed: " + response.message());
                    books.postValue(getCachedBooks());
                }
            }

            @Override
            public void onFailure(Call<GutendexResponse> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
                books.postValue(getCachedBooks());
            }
        });

        return books;
    }
    private List<Book> getCachedBooks() {
        // Implement Room or SharedPreferences to retrieve cached books
        return new ArrayList<>(); // Placeholder
    }


}