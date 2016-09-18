package com.test.testshell;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by Liam on 2016/6/2.
 */
public class OrginalApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        Log.i("demo", this.getClass().getName() + " attachBaseContext");
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        Log.i("demo", this.getClass().getName() + " onCreate");
        super.onCreate();
    }
}
