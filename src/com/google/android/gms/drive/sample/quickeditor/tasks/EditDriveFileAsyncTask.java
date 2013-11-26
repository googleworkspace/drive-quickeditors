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

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFile.OnContentsOpenedCallback;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.MetadataChangeSet;

/**
 * An async task to open, make changes to and close a file.
 */
public abstract class EditDriveFileAsyncTask extends
        AsyncTask<DriveId, Boolean, com.google.android.gms.common.api.Status> {

    private static final String TAG = "EditDriveFileAsyncTask";

    private final GoogleApiClient mClient;

    /**
     * Constructor.
     * @param client A connected {@code GoogleApiClient} instance.
     */
    public EditDriveFileAsyncTask(GoogleApiClient client) {
        mClient = client;
    }

    /**
     * Handles the editing to file metadata and contents.
     */
    public abstract Changes edit(Contents contents);

    /**
     * Opens contents for the given file, executes the editing tasks, saves the
     * metadata and content changes.
     */
    @Override
    protected com.google.android.gms.common.api.Status doInBackground(DriveId... params) {
        DriveFile file = Drive.DriveApi.getFile(mClient, params[0]);
        PendingResult<ContentsResult, OnContentsOpenedCallback> openContentsReq = file
                .openContents(mClient, DriveFile.MODE_WRITE_ONLY, null);
        com.google.android.gms.common.api.Status status = null;
        ContentsResult openContentsResult = openContentsReq.await();
        status = openContentsResult.getStatus();
        if (!status.isSuccess()) {
            return status;
        }

        // apply changes
        Changes changes = edit(openContentsResult.getContents());
        if (changes.getMetadataChangeSet() != null) {
            MetadataResult metadataResult = file.updateMetadata(
                    mClient, changes.getMetadataChangeSet()).await();
            status = metadataResult.getStatus();
            if (!status.isSuccess()) {
                return status;
            }
        }

        if (changes.getContents() != null) {
            status = file.commitAndCloseContents(mClient, changes.getContents()).await();
        }
        return status;
    }

    /**
     * Represents the delta of the metadata changes and keeps a pointer to the
     * file contents to be stored permanently.
     */
    public class Changes {
        private final MetadataChangeSet mMetadataChangeSet;
        private final Contents mContents;

        public Changes(MetadataChangeSet metadataChangeSet, Contents contents) {
            mMetadataChangeSet = metadataChangeSet;
            mContents = contents;
        }

        public MetadataChangeSet getMetadataChangeSet() {
            return mMetadataChangeSet;
        }

        public Contents getContents() {
            return mContents;
        }
    }
}
