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

import com.google.android.gms.Batch;
import com.google.android.gms.Batch.BatchCallback;
import com.google.android.gms.PendingResult;
import com.google.android.gms.Status;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.OnNewContentsCallback;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFile.OnContentsOpenedCallback;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.DriveResource.OnMetadataRetrievedCallback;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.sample.quickeditor.tasks.EditDriveFileAsyncTask;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HomeActivity extends BaseDriveActivity {

  private static final String TAG = "MainActivity";

  private static final int REQUEST_CODE_CREATOR = NEXT_AVAILABLE_REQUEST_CODE;
  private static final int REQUEST_CODE_OPENER = NEXT_AVAILABLE_REQUEST_CODE + 1;

  private static final String MIME_TYPE_TEXT = "text/plain";

  private EditText mTitleEditText;
  private EditText mContentsEditText;
  private Button mSaveButton;

  private DriveId mCurrentDriveId;
  private Metadata mMetadata;
  private Contents mContents;

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
    refresh();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onConnected(Bundle connectionHint) {
    super.onConnected(connectionHint);
    refresh();
  }

  @Override
  public void onConnectionFailed(ConnectionResult result) {
    super.onConnectionFailed(result);
    // TODO: show error
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch(item.getItemId()) {
      case R.id.menu_new:
        Drive.DriveApi.newContents(mGoogleApiClient).addResultCallback(new OnNewContentsCallback() {
          @Override
          public void onNewContents(ContentsResult result) {
            // TODO: error handling in case of failure
            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
              .setMimeType(MIME_TYPE_TEXT)
              .build();
            Intent createIntent = Drive.DriveApi.newCreateFileActivityBuilder(mGoogleApiClient)
                .setInitialMetadata(metadataChangeSet)
                .setInitialContents(result.getContents())
                .build();
            startActivityForResult(createIntent, REQUEST_CODE_CREATOR);
          }
        });
        break;
      case R.id.menu_open:
        Intent i = Drive.DriveApi.newOpenFileActivityBuilder(mGoogleApiClient)
            .setMimeType(new String[]{ MIME_TYPE_TEXT })
            .build();
        startActivityForResult(i, REQUEST_CODE_OPENER);
        break;
    }
    return super.onMenuItemSelected(featureId, item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CODE_CREATOR:
        if (resultCode == RESULT_OK) {
          mCurrentDriveId =
              (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
          refresh();
        }
      case REQUEST_CODE_OPENER:
        if (resultCode == RESULT_OK) {
          mCurrentDriveId =
              (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
          refresh();
          get();
        }
        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void refresh() {
    Log.d(TAG, "Refreshing...");
    if (mCurrentDriveId == null) {
      mSaveButton.setEnabled(false);
      return;
    }
    mSaveButton.setEnabled(true);
  }

  private void get() {
    Log.d(TAG, "Retrieving...");
    DriveFile file = Drive.DriveApi.getFile(mCurrentDriveId);
    final PendingResult<MetadataResult, OnMetadataRetrievedCallback> metadataResult =
        file.getMetadata(mGoogleApiClient);
    final PendingResult<ContentsResult, OnContentsOpenedCallback> contentsResult =
        file.openContents(mGoogleApiClient, DriveFile.MODE_READ_WRITE, null);
    new Batch(metadataResult, contentsResult).addResultCallback(new BatchCallback() {
      @Override
      public void onBatchComplete(Status status) {
        if (!status.getStatus().isSuccess()) {
          showToast(R.string.msg_errretrieval);
          return;
        }
        mMetadata = metadataResult.get().getMetadata();
        mContents = contentsResult.get().getContents();
        mTitleEditText.setText(mMetadata.getTitle());
        mContentsEditText.setText(readFromContents(mContents));
      }
    });

  }

  private void save() {
    Log.d(TAG, "Saving...");
    if (mCurrentDriveId == null) {
      return;
    }

    new EditDriveFileAsyncTask(mGoogleApiClient) {
      @Override
      public Changes edit(Contents contents) {
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
            .setTitle(mTitleEditText.getText().toString())
            .build();
        writeContents(contents, mContentsEditText.getText().toString());
        return new Changes(metadataChangeSet, contents);
      }

      @Override
      protected void onPostExecute(com.google.android.gms.Status status) {
        // TODO: handle error, fix com.google.android.gms.Status ugliness.
        showToast(R.string.msg_saved);
      }
    }.execute(mCurrentDriveId);
  }

  private void showToast(int id) {
    Toast.makeText(this, id, Toast.LENGTH_LONG).show();
  }

  private String readFromContents(Contents contents) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
    StringBuilder builder = new StringBuilder();
    try {
      String line;
      while ((line = reader.readLine()) != null) {
          builder.append(line);
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return builder.toString();
  }

  private void writeContents(Contents contents, String text) {
    try {
      contents.getOutputStream().write(text.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
