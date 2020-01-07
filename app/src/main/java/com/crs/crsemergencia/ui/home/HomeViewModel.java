package com.crs.crsemergencia.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.crs.crsemergencia.userParcelable;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private userParcelable user;
    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Módulo de llamada");
    }

    public LiveData<String> getText() {
        return mText;
    }
}