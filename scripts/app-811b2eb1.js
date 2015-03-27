"use strict";/*
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
function on_gapi_loaded(){window.init_gapi?window.init_gapi():setTimeout(on_gapi_loaded,10)}/*
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
var module=angular.module("editor.rename",["ngMaterial"]);module.controller("RenameCtrl",["$scope","$mdDialog","title",function(e,t,n){e.form={title:n},this.save=function(){t.hide(e.form.title)},this.cancel=function(){t.cancel()}}]),/*
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
angular.module("editor.rename").service("renameDialog",["$mdDialog",function(e){this.show=function(t,n){return e.show({targetEvent:t,templateUrl:"components/rename/rename.html",controller:"RenameCtrl",controllerAs:"ctrl",locals:{title:n}})}}]);/*
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
var module=angular.module("editor.login",["editor.gapi","ngMaterial"]);module.controller("LoginCtrl",["$mdDialog","login","user",function(e,t,n){this.login=function(){t.login(n).then(function(){e.hide()})}}]),angular.module("editor.login").service("login",["$q","$mdDialog","googleApi","clientId","scope",function(e,t,n,i,o){var l=function(){var e=gapi.auth.getToken();return e&&Date.now()<e.expires_at},r=function(e,t){var n={client_id:i,scope:o,immediate:e};return t&&(n.login_hint=t,n.authuser=-1),n},a=function(t){return n.then(function(n){if(l())return n.auth.getToken();var i=e.defer();return n.auth.authorize(t,function(e){if(e&&!e.error)i.resolve(e);else{var t=e?e.error:"Unknown authentication error";i.reject(t)}}),i.promise})};this.login=function(e){var t=r(!1,e);return a(t)},this.checkAuth=function(e){var t=r(!0,e);return a(t)},this.showLoginDialog=function(e,n){return t.show({targetEvent:e,templateUrl:"components/login/login.html",controller:"LoginCtrl",clickOutsideToClose:!1,escapeToClose:!1,controllerAs:"ctrl",locals:{user:n}})}}]);var module=angular.module("editor.gapi",[]);module.factory("googleApi",["$rootScope","$window","$q","apiKey","loadApis",function(e,t,n,i,o){var l=n.defer();return t.init_gapi=function(){e.$apply(function(){var e=[];i&&t.gapi.client.setApiKey(i),angular.forEach(o,function(t,i){e.push(n.when(gapi.client.load(i,t)))}),n.all(e).then(function(){l.resolve(t.gapi)})})},l.promise}]);var MultiPartBuilder=function(){this.boundary=Math.random().toString(36).slice(2),this.mimeType='multipart/mixed; boundary="'+this.boundary+'"',this.parts=[],this.body=null};MultiPartBuilder.prototype.append=function(e,t){if(null!==this.body)throw new Error("Builder has already been finalized.");return this.parts.push("\r\n--",this.boundary,"\r\n","Content-Type: ",e,"\r\n\r\n",t),this},MultiPartBuilder.prototype.finish=function(){if(0===this.parts.length)throw new Error("No parts have been added.");return null===this.body&&(this.parts.push("\r\n--",this.boundary,"--"),this.body=this.parts.join("")),{type:this.mimeType,body:this.body}};/*
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
var module=angular.module("editor.drive",["editor.gapi"]);module.service("drive",["$q","$cacheFactory","googleApi","applicationId",function(e,t,n,i){var o="id,title,mimeType,userPermission,editable,copyable,shared,fileSize",l=t("files"),r=function(e,t){var n={metadata:e,content:t};return l.put(e.id,n),n};this.loadFile=function(t){var i=l.get(t);return i?e.when(i):n.then(function(n){var i=n.client.drive.files.get({fileId:t,fields:o}),l=n.client.drive.files.get({fileId:t,alt:"media"});return e.all([e.when(i),e.when(l)])}).then(function(e){return r(e[0].result,e[1].body)})},this.saveFile=function(t,i){return n.then(function(n){var l,r;t.id?(l="/upload/drive/v2/files/"+encodeURIComponent(t.id),r="PUT"):(l="/upload/drive/v2/files",r="POST");var a=(new MultiPartBuilder).append("application/json",JSON.stringify(t)).append(t.mimeType,i).finish(),d=n.client.request({path:l,method:r,params:{uploadType:"multipart",fields:o},headers:{"Content-Type":a.type},body:a.body});return e.when(d)}).then(function(e){return r(e.result,i)})},this.showPicker=function(){return n.then(function(t){var n=e.defer(),o=new google.picker.View(google.picker.ViewId.DOCS);o.setMimeTypes("text/plain");var l=(new google.picker.PickerBuilder).setAppId(i).setOAuthToken(t.auth.getToken().access_token).addView(o).setCallback(function(e){if("picked"==e.action){var t=e.docs[0].id;n.resolve(t)}else"cancel"==e.action&&n.reject()}).build();return l.setVisible(!0),n.promise})},this.showSharing=function(e){return n.then(function(t){var n=new t.drive.share.ShareClient(i);n.setItemIds([e]),n.showSettingsDialog()})}}]),/*
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
angular.module("editor",["editor.login","editor.rename","editor.drive","ngRoute","ngMaterial","ui.codemirror"]).constant("apiKey",null).constant("clientId","709207149709-fadikftqudacphtr4pq5mu80s6tqklrb.apps.googleusercontent.com").constant("applicationId","709207149709").constant("scope",["email","profile","https://www.googleapis.com/auth/drive","https://www.googleapis.com/auth/drive.install"]).constant("loadApis",{drive:"v2"}).config(["$routeProvider",function(e){e.when("/edit/:fileId?",{templateUrl:"app/main/main.html",controller:"MainCtrl",controllerAs:"ctrl"}).otherwise({redirectTo:function(){return console.log("Otherwise..."),"/edit/"}})}]);/*
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
var module=angular.module("editor");module.controller("MainCtrl",["$scope","$location","$routeParams","$q","$mdToast","drive","login","renameDialog",function(e,t,n,i,o,l,r,a){var d={content:"",metadata:{id:null,title:"untitled.txt",mimeType:"text/plain",editable:!0}};e.file=null,e.loading=!0;var c=function(e){o.show(o.simple().content(e))},u=function(){return l.saveFile(e.file.metadata,e.file.content).then(function(t){return m(t.metadata.id),e.file=t,c("File saved"),e.file},function(e){return c("Unable to save file"),i.reject(e)})},s=function(t){var n=t?l.loadFile(t):i.when(d);return n.then(function(t){return e.file=t,e.file},function(){return t&&c("Unable to load file"),s()})},m=function(n){e.file.metadata.id!=n&&(t.path("/edit/"+n),t.search(""),t.replace())};this.saveFile=function(t){return null===e.file.metadata.id?this.renameFile(t):u()},this.renameFile=function(t){return a.show(t,e.file.metadata.title).then(function(t){return e.file.metadata.title=t,u()})},this.openFile=function(){l.showPicker().then(function(e){m(e)})},this.shareFile=function(t){null===e.file.metadata.id?e.ctrl.renameFile(t).then(function(){l.showSharing(e.file.metadata.id)}):l.showSharing(e.file.metadata.id)};var p=angular.bind(e.ctrl,s,n.fileId);r.checkAuth(n.user).then(p,function(){return r.showLoginDialog(null,n.user).then(p)}).finally(function(){e.loading=!1})}]),angular.module("editor").run(["$templateCache",function(e){e.put("app/main/main.html",'<md-toolbar ng-hide="loading" layout="row"><div class="md-toolbar-tools"><span flex="" ng-click="ctrl.renameFile($event)"><span>{{file.metadata.title}}<md-tooltip>Click to rename</md-tooltip></span></span><md-button ng-click="ctrl.shareFile($event)" class="toolbar-action" aria-label="Share file"><md-icon alt="share" md-font-icon="mdi mdi-account-multiple"></md-icon><md-tooltip>Share file</md-tooltip></md-button><md-button ng-click="ctrl.saveFile($event)" class="toolbar-action" aria-label="Save file to Google Drive"><md-icon alt="save" md-font-icon="mdi mdi-content-save"></md-icon><md-tooltip>Save file to Google Drive&#0153;</md-tooltip></md-button><md-button ng-click="ctrl.openFile($event)" class="toolbar-action" hide-sm="" aria-label="Open file from Google Drive"><md-icon alt="open" md-font-icon="mdi mdi-google-drive"></md-icon><md-tooltip>Open file from Google Drive&#0153;</md-tooltip></md-button></div></md-toolbar><div ng-show="loading" layout-align="center center" flex="" layout="column"><md-progress-circular md-mode="indeterminate"></md-progress-circular></div><md-content ng-hide="loading" layout-margin="" flex="" class="editor-frame md-whiteframe-z1"><ui-codemirror flex="" ng-model="file.content" ui-refresh="loading"></ui-codemirror></md-content>'),e.put("components/login/login.html",'<md-dialog><md-content><p>Please sign-in with your Google account to continue.</p></md-content><div class="md-actions" layout="row" layout-align="center center"><md-button class="md-primary" ng-click="ctrl.login()">Sign-in</md-button></div></md-dialog>'),e.put("components/rename/rename.html",'<md-dialog><md-content layout-padding="" layout="row" layout-sm="column"><md-input-container><label>Title</label> <input ng-model="form.title"></md-input-container></md-content><div class="md-actions" layout="row" layout-align="center center"><md-button ng-click="ctrl.cancel()">Cancel</md-button><md-button ng-click="ctrl.save()">Save</md-button></div></md-dialog>')}]);