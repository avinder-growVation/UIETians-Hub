package com.medha.avinder.uietianshub.puWifi.widget;

import android.app.IntentService;
import android.content.Intent;

import com.medha.avinder.uietianshub.puWifi.Functions;
import com.medha.avinder.uietianshub.puWifi.LoginTask;

/**
 * Created by Simar Arora on 2/14/2015.
 *This App is Licensed under GNU General Public License. A copy of this license can be found in the root of this project.
 */
public class LoginService extends IntentService {

    public LoginService(String name) {
        super(name);
    }

    public LoginService() {
        super("LoginService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!Functions.isInitialised(this))
            return;
        new LoginTask(this, false).execute();
        this.stopSelf();
    }
}

