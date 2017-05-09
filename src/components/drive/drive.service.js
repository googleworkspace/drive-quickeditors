'use strict';

/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var module = angular.module('editor.drive', ['editor.gapi']);

module.service('drive', ['$q', '$cacheFactory', 'googleApi', 'applicationId', function($q, $cacheFactory, googleApi, applicationId) {

  // Only fetch fields that we care about
  var DEFAULT_FIELDS = 'id,title,mimeType,userPermission,editable,copyable,shared,fileSize';

  var cache = $cacheFactory('files');

  /**
   * Combines metadata & content into a single object & caches the result
   *
   * @param {Object} metadata File metadata
   * @param {String} content File content
   * @return {Object} combined object
   */
  var combineAndStoreResults = function(metadata, content) {
    var file = {
      metadata: metadata,
      content: content
    };
    cache.put(metadata.id, file);
    return file;
  };

  /**
   * Load a file from Drive. Fetches both the metadata & content in parallel.
   *
   * @param {String} fileID ID of the file to load
   * @return {Promise} promise that resolves to an object containing the file metadata & content
   */
  this.loadFile = function(fileId) {
    var file = cache.get(fileId);
    if (file) {
      return $q.when(file);
    }
    return googleApi.then(function(gapi) {
      var metadataRequest = gapi.client.drive.files.get({
        fileId: fileId,
        supportsTeamDrives: true,
        fields: DEFAULT_FIELDS
      });
      var contentRequest = gapi.client.drive.files.get({
        fileId: fileId,
        supportsTeamDrives: true,
        alt: 'media'
      });
      return $q.all([$q.when(metadataRequest), $q.when(contentRequest)]);
    }).then(function(responses) {
      return combineAndStoreResults(responses[0].result, responses[1].body);
    });
  };

 /**
   * Save a file to Drive using the mutlipart upload protocol.
   *
   * @param {Object} metadata File metadata to save
   * @param {String} content File content
   * @return {Promise} promise that resolves to an object containing the current file metadata & content
   */
  this.saveFile = function(metadata, content) {
    return googleApi.then(function(gapi) {
      var path;
      var method;

      if (metadata.id) {
        path = '/upload/drive/v2/files/' + encodeURIComponent(metadata.id);
        method = 'PUT';
      } else {
        path = '/upload/drive/v2/files';
        method = 'POST';
      }

      var multipart = new MultiPartBuilder()
        .append('application/json', JSON.stringify(metadata))
        .append(metadata.mimeType, content)
        .finish();

      var uploadRequest = gapi.client.request({
        path: path,
        method: method,
        params: {
          uploadType: 'multipart',
          supportsTeamDrives: true,
          fields: DEFAULT_FIELDS
        },
        headers: { 'Content-Type' : multipart.type },
        body: multipart.body
      });
      return $q.when(uploadRequest);
    }).then(function(response) {
      return combineAndStoreResults(response.result, content);
    });
  };

  /**
   * Displays the Drive file picker configured for selecting text files
   *
   * @return {Promise} Promise that resolves with the ID of the selected file
   */
  this.showPicker = function() {
    return googleApi.then(function(gapi) {
      var deferred = $q.defer();
      var view = new google.picker.View(google.picker.ViewId.DOCS);
      view.setMimeTypes('text/plain');
      var picker = new google.picker.PickerBuilder()
        .setAppId(applicationId)
        .setOAuthToken(gapi.auth.getToken().access_token)
        .addView(view)
        .setCallback(function(data) {
          if (data.action == 'picked') {
            var id = data.docs[0].id;
            deferred.resolve(id);
          } else if (data.action == 'cancel') {
            deferred.reject();
          }
        })
        .build();
      picker.setVisible(true);
      return deferred.promise;
    });
  };

  /**
   * Displays the Drive sharing dialog
   *
   * @param {String} id ID of the file to share
   */
  this.showSharing = function(id) {
    return googleApi.then(function(gapi) {
      //var deferred = $q.defer();
      var share = new gapi.drive.share.ShareClient(applicationId);
      share.setItemIds([id]);
      share.showSettingsDialog();
      //return deferred.promise;
    });
  };

}]);
