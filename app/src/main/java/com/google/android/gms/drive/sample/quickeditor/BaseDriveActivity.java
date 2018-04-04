/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.drive.sample.quickeditor;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * An abstract activity that handles authorization and connection to the Drive services.
 */
public abstract class BaseDriveActivity extends Activity {

    private static final String TAG = "BaseDriveActivity";

    /**
     * Dictionary key for {@link BaseDriveActivity#mAccountName}.
     */
    protected static final String ACCOUNT_NAME_KEY = "account_name";

    /**
     * Sign-in request code.
     */
    private static final int REQUEST_CODE_SIGN_IN = 0;

    /**
     * Next available request code for child classes.
     */
    protected static final int NEXT_AVAILABLE_REQUEST_CODE = 1;

    /**
     * Google sign-in client.
     */
    protected GoogleSignInClient mGoogleSignInClient;

    /**
     * Google Drive client.
     */
    protected DriveClient mDriveClient;

    /**
     * Google Drive resource client.
     */
    protected DriveResourceClient mDriveResourceClient;

    /**
     * Selected account name to authorize the app for and authenticate the client with.
     */
    protected String mAccountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isSignedIn()) {
            signIn();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ACCOUNT_NAME_KEY, mAccountName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAccountName = savedInstanceState.getString(ACCOUNT_NAME_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Log.i(TAG, "Sign-in request code.");
            // Called after user is signed in.
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Signed in successfully.");
                // Create Drive clients now that account has been authorized access.
                createDriveClients(GoogleSignIn.getLastSignedInAccount(this));
            } else {
                Log.w(TAG, String.format("Unable to sign in, result code %d", resultCode));
            }
        }
    }

    public boolean isSignedIn() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        return mGoogleSignInClient != null
                && (signInAccount != null
                && signInAccount.getGrantedScopes().contains(Drive.SCOPE_FILE));
    }

    /**
     * Attempts silent sign-in. On failure, start a sign-in {@link Intent}.
     */
    private void signIn() {
        Log.i(TAG, "Start sign-in.");
        mGoogleSignInClient = getGoogleSignInClient();
        // Attempt silent sign-in
        mGoogleSignInClient.silentSignIn()
            .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                @Override
                public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                    createDriveClients(googleSignInAccount);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Silent sign-in failed, display account selection prompt
                    startActivityForResult(
                        mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                }
            });
    }

    /**
     * Builds a Google sign-in client.
     */
    private GoogleSignInClient getGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    /**
     * Builds the Drive clients after successful sign-in.
     *
     * @param googleSignInAccount The account which was signed in to.
     */
    private void createDriveClients(GoogleSignInAccount googleSignInAccount) {
        Log.i(TAG, "Update view with sign-in account.");
        // Build a drive client.
        mDriveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
        // Build a drive resource client.
        mDriveResourceClient =
            Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
    }
}
