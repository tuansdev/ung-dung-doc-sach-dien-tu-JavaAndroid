package com.example.ebookreader.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;
import com.example.ebookreader.data.AppDatabase;
import com.example.ebookreader.data.User;

public class AuthViewModel extends AndroidViewModel {
    private final AppDatabase db;

    public AuthViewModel(Application application) {
        super(application);
        db = Room.databaseBuilder(application, AppDatabase.class, "app-database").build();
    }

    public LiveData<User> login(String email, String password) {
        MutableLiveData<User> result = new MutableLiveData<>();
        new Thread(() -> {
            User user = db.userDao().getUser(email, password);
            result.postValue(user);
        }).start();
        return result;
    }

    public LiveData<Boolean> register(String email, String password) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        new Thread(() -> {
            try {
                User user = new User();
                user.email = email;
                user.password = password;
                db.userDao().insert(user);
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        }).start();
        return result;
    }
}