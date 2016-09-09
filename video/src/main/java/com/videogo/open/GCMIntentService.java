/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.videogo.open;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.videogo.androidpn.CommonUtilities;
import com.videogo.androidpn.ServerUtilities;
import com.videogo.util.LogUtil;

public class GCMIntentService extends GCMBaseIntentService {

    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";
    
    public GCMIntentService() {
        super(CommonUtilities.SENDER_ID);
    }

    @Override
    public void onCreate() {
        LogUtil.infoLog(TAG, "onCreate... ");
        super.onCreate();
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        LogUtil.infoLog(TAG, "Device registered: regId = " + registrationId);
        //CommonUtilities.displayMessage(context, getString(R.string.gcm_registered));
        ServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        LogUtil.infoLog(TAG, "Device unregistered");
        //CommonUtilities.displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            LogUtil.infoLog(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String message = intent.getExtras().getString("message");
        String ext = intent.getExtras().getString("ext");
        LogUtil.infoLog(TAG, "Received message : message = " + message + ",ext = " + ext);
        // String message = getString(R.string.gcm_message);
        CommonUtilities.displayMessage(context, message, ext);
        // notifies user
        // generateNotification(context, message);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        LogUtil.infoLog(TAG, "Received deleted messages notification");
        //String message = getString(R.string.gcm_deleted, total);
        //CommonUtilities.displayMessage(context, message);
        // notifies user
        // generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        LogUtil.infoLog(TAG, "Received error: " + errorId);
        //CommonUtilities.displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        LogUtil.infoLog(TAG, "Received recoverable error: " + errorId);
        //CommonUtilities.displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
        return super.onRecoverableError(context, errorId);
    }
}
