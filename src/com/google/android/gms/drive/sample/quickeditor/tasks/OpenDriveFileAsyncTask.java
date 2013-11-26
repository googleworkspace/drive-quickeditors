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

package com.google.android.gms.drive.sample.quickeditor.tasks;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.sample.quickeditor.Utils;
import com.google.android.gms.drive.sample.quickeditor.tasks.OpenDriveFileAsyncTask.MetadataAndContents;

/**
 * An async task to open, make changes to and close a file.
 */
public abstract class OpenDriveFileAsyncTask
        extends AsyncTask<DriveId, Boolean, MetadataAndContents> {

    private static final String TAG = "OpenDriveFileAsyncTask";

    private final GoogleApiClient mClient;

    /**
     * Constructor.
     * @param client A connected {@code GoogleApiClient} instance.
     */
    public OpenDriveFileAsyncTask(GoogleApiClient client) {
        mClient = client;
    }

    /**
     * Opens contents for the given file, executes the editing tasks, saves the
     * metadata and content changes.
     */
    @Override
    protected MetadataAndContents doInBackground(DriveId... params) {
        DriveFile file = Drive.DriveApi.getFile(mClient, params[0]);
        MetadataResult metadataResult = file.getMetadata(mClient).await();
        if (!metadataResult.getStatus().isSuccess()) {
            return null;
        }
        ContentsResult contentsResult =
                file.openContents(mClient, DriveFile.MODE_READ_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            return null;
        }
        String contentsAsString = null;
        try {
            contentsAsString = Utils.readFromInputStream(
                    contentsResult.getContents().getInputStream());
        } catch (IOException e) {
            Log.e(TAG, "Cannot read from the input stream.", e);
        }
        file.discardContents(mClient, contentsResult.getContents()).await();
        return new MetadataAndContents(metadataResult.getMetadata(), contentsAsString);
    }

    public class MetadataAndContents {
        private final Metadata mMetadata;
        private final String mContents;

        public MetadataAndContents(Metadata metadata, String contents) {
            mMetadata = metadata;
            mContents = contents;
        }

        public Metadata getMetadata() {
            return mMetadata;
        }

        public String getContents() {
            return mContents;
        }
    }
}
