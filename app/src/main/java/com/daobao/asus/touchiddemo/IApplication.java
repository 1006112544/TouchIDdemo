package com.daobao.asus.touchiddemo;

import android.app.Application;

/**
 * Created by db on 2018/8/19.
 */
public class IApplication extends android.app.Application {
    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }


    public static Application getApplication() {
        return application;
    }
}
