/**
 * Copyright 2022 Google LLC
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

'use strict';

/**
 * Helper for building multipart requests for uploading to Drive.
 */
var MultiPartBuilder = function() {
  this.boundary = Math.random().toString(36).slice(2);
  this.mimeType = 'multipart/mixed; boundary="' + this.boundary + '"';
  this.parts = [];
  this.body = null;
};

/**
 * Appends a part.
 *
 * @param {String} mimeType Content type of this part
 * @param {Blob|File|String} content Body of this part
 */
MultiPartBuilder.prototype.append = function(mimeType, content) {
  if(this.body !== null) {
    throw new Error("Builder has already been finalized.");
  }
  this.parts.push(
    "\r\n--", this.boundary, "\r\n",
    "Content-Type: ", mimeType, "\r\n\r\n",
    content);
  return this;
};

/**
 * Finalizes building of the multipart request and returns a Blob containing
 * the request. Once finalized, appending additional parts will result in an
 * error.
 *
 * @returns {Object} Object containing the mime type (mimeType) & assembled multipart body (body)
 */
MultiPartBuilder.prototype.finish = function() {
  if (this.parts.length === 0) {
    throw new Error("No parts have been added.");
  }
  if (this.body === null) {
    this.parts.push("\r\n--", this.boundary, "--");
    this.body = this.parts.join('');
    // TODO - switch to blob once gapi.client.request allows it
    // this.body = new Blob(this.parts, {type: this.mimeType});
  }
  return {
    type: this.mimeType,
    body: this.body
  };
};
