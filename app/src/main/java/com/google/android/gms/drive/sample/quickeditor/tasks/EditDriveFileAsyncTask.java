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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.ExecutionException;

/**
 * An async task to open, make changes to and close a file.
 */
public abstract class EditDriveFileAsyncTask
        extends AsyncTask<DriveId, Boolean, Boolean> {

    /**
     * Represents the delta of the metadata changes and keeps a pointer to the file
     * contents to be stored permanently.
     */
    public class Changes {
        private MetadataChangeSet mMetadataChangeSet;
        private DriveContents mDriveContents;

        public Changes(MetadataChangeSet metadataChangeSet, DriveContents contents) {
            mMetadataChangeSet = metadataChangeSet;
            mDriveContents = contents;
        }

        public MetadataChangeSet getMetadataChangeSet() {
            return mMetadataChangeSet;
        }

        public DriveContents getDriveContents() {
            return mDriveContents;
        }
    }

    private static final String TAG = "EditDriveFileAsyncTask";

    private DriveResourceClient driveResourceClient;

    /**
     * Constructor.
     *
     * @param driveResourceClient A connected {@link DriveResourceClient} instance.
     */
    public EditDriveFileAsyncTask(DriveResourceClient driveResourceClient) {
        this.driveResourceClient = driveResourceClient;
    }

    /**
     * Handles the editing to file metadata and contents.
     */
    public abstract Changes edit(DriveContents driveContents);

    /**
     * Opens contents for the given file, executes the editing tasks, saves the
     * metadata and content changes.
     */
    @Override
    protected Boolean doInBackground(DriveId... params) {
        DriveFile file = params[0].asDriveFile();
        try {
            DriveContents contents =
                Tasks.await(driveResourceClient.openFile(file, DriveFile.MODE_WRITE_ONLY));

            Changes changes = edit(contents);
            MetadataChangeSet changeSet = changes.getMetadataChangeSet();
            DriveContents updatedContents = changes.getDriveContents();

            if (changeSet != null) {
                Tasks.await(driveResourceClient.updateMetadata(file, changeSet));
            }

            if (updatedContents != null) {
                Tasks.await(driveResourceClient.commitContents(updatedContents, changeSet));
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error editing DriveFile.", e);
            return false;
        }

        return true;
    }
}
