/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.drive.sample.quickeditor;

import com.google.android.gms.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.drive.Drive;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * An abstract activity that handles auth and connection to the Drive services.
 */
public abstract class BaseDriveActivity extends Activity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private static final String TAG = "BaseDriveActivity";

  protected static final String EXTRA_ACCOUNT_NAME = "accountName";

  protected static final int REQUEST_CODE_RESOLUTION = 1;

  protected static final int NEXT_AVAILABLE_REQUEST_CODE = 2;

  // This variable should only be accessed from the UI thread.
  protected GoogleApiClient mGoogleApiClient;

  protected String mAccountName;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    if (bundle != null) {
      mAccountName = bundle.getString(EXTRA_ACCOUNT_NAME);
    }
    if (mAccountName == null) {
      mAccountName = getIntent().getStringExtra(EXTRA_ACCOUNT_NAME);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(EXTRA_ACCOUNT_NAME, mAccountName);
  }

  /**
   * Called when activity gets visible. A connection to Drive
   * services need to be initiated as soon as the activity is visible.
   * Registers {@code ConnectionCallbacks} and {@code OnConnectionFailedListener}
   * on the activities itself.
   */
  @Override
  protected void onResume() {
    super.onResume();
    Log.i(TAG, "Connecting with " + mAccountName);
    if (mAccountName == null) {
      Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
      if (accounts.length == 0) {
        Log.d(TAG, "Must have a Google account installed");
        return;
      }
      mAccountName = accounts[0].name;
    }

    if (mGoogleApiClient == null) {
      mGoogleApiClient = new GoogleApiClient.Builder(this)
          .addApi(Drive.API)
          .addScope(Drive.SCOPE_FILE)
          .setAccountName(mAccountName)
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListeners(this)
          .build();
    }
    mGoogleApiClient.connect();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
      mGoogleApiClient.connect();
    }
  }

  /**
   * Called when activity gets invisible. Connection to Drive
   * service needs to be disconnected as soon as an activity
   * is invisible.
   */
  @Override
  protected void onPause() {
    if (mGoogleApiClient != null) {
      Log.i(TAG, "Disconnecting on pause");
      mGoogleApiClient.disconnect();
    }
    super.onPause();
  }

  /**
   * Called when {@code mGoogleApiClient} is connected.
   */
  @Override
  public void onConnected(Bundle connectionHint) {
    Log.i(TAG, "GoogleApiClient connected");
  }

  /**
   * Called when {@code mGoogleApiClient} is disconnected.
   */
  @Override
  public void onDisconnected() {
    Log.i(TAG, "GoogleApiClient disconnected");
  }

  /**
   * Called when {@code mGoogleApiClient} is trying to connect but failed.
   * Handle {@code result.getResolution()} if there is a resolution is
   * available.
   */
  @Override
  public void onConnectionFailed(ConnectionResult result) {
    Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
    if (result.hasResolution()) {
      try {
        //result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        result.getResolution().send(REQUEST_CODE_RESOLUTION);
      } catch (CanceledException e) {
        // TODO(burcud): Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
