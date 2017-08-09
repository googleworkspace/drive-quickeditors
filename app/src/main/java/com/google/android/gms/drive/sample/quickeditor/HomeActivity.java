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

import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.sample.quickeditor.tasks.EditDriveFileAsyncTask;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collections;

/**
 * An activity that lets you open/create a Drive text file and modify it.
 */
public class HomeActivity extends BaseDriveActivity {

    private static final String TAG = "MainActivity";

    /**
     * Request code for file creator activity.
     */
    private static final int REQUEST_CODE_CREATOR = NEXT_AVAILABLE_REQUEST_CODE;

    /**
     * Request code for the file opener activity.
     */
    private static final int REQUEST_CODE_OPENER = NEXT_AVAILABLE_REQUEST_CODE + 1;

    /**
     * Text file MIME type.
     */
    private static final String MIME_TYPE_TEXT = "text/plain";

    /**
     * Title edit text field.
     */
    private EditText mTitleEditText;

    /**
     * Body edit text field.
     */
    private EditText mContentsEditText;

    /**
     * Save button. Invokes the upsert tasks on click.
     */
    private Button mSaveButton;

    /**
     * Drive ID of the currently opened Drive file.
     */
    private DriveId mCurrentDriveId;

    /**
     * Currently opened file's metadata.
     */
    private Metadata mMetadata;

    /**
     * Currently opened file's contents.
     */
    private DriveContents mDriveContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitleEditText = (EditText) findViewById(R.id.editTextTitle);
        mContentsEditText = (EditText) findViewById(R.id.editTextContents);
        mSaveButton = (Button) findViewById(R.id.buttonSave);
        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        refreshUiFromCurrentFile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            Log.i(TAG, "Create file selected.");
            createDriveFile();
        } else if (item.getItemId() == R.id.menu_open) {
            Log.i(TAG, "Open file selected.");
            openDriveFile();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
            case REQUEST_CODE_OPENER:
                if (resultCode == RESULT_OK) {
                    mCurrentDriveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    loadCurrentFile();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Refreshes the main content view with the current activity state.
     */
    private void refreshUiFromCurrentFile() {
        Log.d(TAG, "Refreshing...");
        if (mCurrentDriveId == null) {
            mSaveButton.setEnabled(false);
            return;
        }
        mSaveButton.setEnabled(true);

        if (mMetadata == null || mDriveContents == null) {
            return;
        }

        mTitleEditText.setText(mMetadata.getTitle());
        try {
            String contents = Utils.readFromInputStream(mDriveContents.getInputStream());
            mContentsEditText.setText(contents);
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading from contents input stream", e);
            showToast(R.string.msg_errreading);
            mSaveButton.setEnabled(false);
        }
    }

    /**
     * Retrieves the currently selected Drive file's metadata and contents.
     */
    private void loadCurrentFile() {
        Log.d(TAG, "Retrieving...");
        final DriveFile file = mCurrentDriveId.asDriveFile();

        // Retrieve and store the file metadata and contents.
        mDriveResourceClient.getMetadata(file)
            .continueWithTask(new Continuation<Metadata, Task<DriveContents>>() {
                @Override
                public Task<DriveContents> then(@NonNull Task<Metadata> task) {
                    if (task.isSuccessful()) {
                        mMetadata = task.getResult();
                        return mDriveResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);
                    } else {
                        return Tasks.forException(task.getException());
                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<DriveContents>() {
                @Override
                public void onSuccess(DriveContents driveContents) {
                    mDriveContents = driveContents;
                    refreshUiFromCurrentFile();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Unable to retrieve file metadata and contents.", e);
                }
            });
    }

    /**
     * Saves metadata and content changes.
     */
    private void save() {
        Log.d(TAG, "Saving...");

        if (mCurrentDriveId == null) {
            return;
        }

        new EditDriveFileAsyncTask(mDriveResourceClient) {
            @Override
            public Changes edit(DriveContents driveContents) {
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setTitle(mTitleEditText.getText().toString())
                        .build();
                try {
                    byte[] body = mContentsEditText.getText().toString().getBytes();
                    driveContents.getOutputStream().write(body);
                } catch (IOException e) {
                    Log.e(TAG, "IOException while reading from driveContents output stream", e);
                }
                return new Changes(metadataChangeSet, driveContents);
            }

            @Override
            protected void onPostExecute(Boolean isSuccess) {
                if (isSuccess) {
                    showToast(R.string.msg_saved);
                } else {
                    showToast(R.string.msg_errsaving);
                }
            }
        }.execute(mCurrentDriveId);
    }

    /**
     * Shows a {@link Toast} with the given message.
     */
    private void showToast(int id) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show();
    }

    /**
     * Launches an {@link Intent} to create a new Drive file.
     */
    private void createDriveFile() {
        Log.i(TAG, "Create drive file.");

        if (!isSignedIn()) {
            Log.w(TAG, "Failed to create file, user is not signed in.");
            return;
        }

        // Nullify the previous DriveContents and Metadata
        mDriveContents = null;
        mMetadata = null;

        // Build the DriveContents and start a CreateFileActivityIntent.
        mDriveResourceClient.createContents()
            .continueWithTask(new Continuation<DriveContents, Task<IntentSender>>() {
                @Override
                public Task<IntentSender> then(@NonNull Task<DriveContents> task) {
                    if (!task.isSuccessful()) {
                        return Tasks.forException(task.getException());
                    }
                    Log.i(TAG, "New contents created.");
                    // Build file metadata options.
                    MetadataChangeSet metadataChangeSet =
                        new MetadataChangeSet.Builder()
                            .setMimeType(MIME_TYPE_TEXT)
                            .build();
                    // Build file creation options.
                    CreateFileActivityOptions createFileActivityOptions =
                        new CreateFileActivityOptions.Builder()
                            .setInitialMetadata(metadataChangeSet)
                            .setInitialDriveContents(task.getResult())
                            .build();
                    // Build CreateFileActivityIntent.
                    return mDriveClient.newCreateFileActivityIntentSender(createFileActivityOptions);
                }
            }).addOnSuccessListener(new OnSuccessListener<IntentSender>() {
                  @Override
                  public void onSuccess(IntentSender intentSender) {
                      Log.i(TAG, "New CreateActivityIntent created.");
                      try {
                          // Start CreateFileActivityIntent
                          startIntentSenderForResult(
                              intentSender,
                              REQUEST_CODE_CREATOR,
                              /* fillInIntent= */ null,
                              /* flagsMask= */ 0,
                              /* flagsValues= */ 0,
                              /* extraFlags= */ 0);
                      } catch (SendIntentException e) {
                          Log.e(TAG, "Failed to launch file chooser.", e);
                      }
                  }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to create file.", e);
                }
            });
    }

    /**
     * Launches an {@link Intent} to open an existing Drive file.
     */
    private void openDriveFile() {
        Log.i(TAG, "Open Drive file.");

        if (!isSignedIn()) {
            Log.w(TAG, "Failed to open file, user is not signed in.");
            return;
        }

        // Build activity options.
        final OpenFileActivityOptions openFileActivityOptions =
            new OpenFileActivityOptions.Builder()
                .setMimeType(Collections.singletonList(MIME_TYPE_TEXT))
                .build();

        // Start a OpenFileActivityIntent
        mDriveClient.newOpenFileActivityIntentSender(openFileActivityOptions)
            .addOnSuccessListener(new OnSuccessListener<IntentSender>() {
                @Override
                public void onSuccess(IntentSender intentSender) {
                    try {
                        startIntentSenderForResult(
                            intentSender,
                            REQUEST_CODE_OPENER,
                            /* fillInIntent= */ null,
                            /* flagsMask= */ 0,
                            /* flagsValues= */ 0,
                            /* extraFlags= */ 0);
                    } catch (SendIntentException e) {
                        Log.w(TAG, "Unable to send intent.", e);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Unable to create OpenFileActivityIntent.", e);
                }
            });
    }
}
