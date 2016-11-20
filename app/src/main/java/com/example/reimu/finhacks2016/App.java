package com.example.reimu.finhacks2016;

import android.app.Application;
import android.content.Context;

/**
 * Created by mihai on 2016-11-19.
 */

public class App extends Application {
    private static App mInstance;
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static App getInstance(){
        return mInstance;
    }

    public static Context getAppContext() {
        return mAppContext;
    }

    public void setAppContext(Context mAppContext) {
        this.mAppContext = mAppContext;
    }
}

