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

// Configure our app
angular.module('editor', ['editor.login', 'editor.rename', 'editor.drive', 'ngRoute', 'ngMaterial', 'ui.codemirror'])
  .constant('apiKey', null)
  .constant('clientId', '709207149709-fadikftqudacphtr4pq5mu80s6tqklrb.apps.googleusercontent.com')
  .constant('applicationId', '709207149709')
  .constant('scope', ['email', 'profile', 'https://www.googleapis.com/auth/drive', 'https://www.googleapis.com/auth/drive.install'])
  .constant('loadApis', {
    'drive' : 'v2'
  })
  .config(function ($routeProvider) {
    $routeProvider
      .when('/edit/:fileId?', {
        templateUrl: 'main/main.html',
        controller: 'MainCtrl',
        controllerAs: 'ctrl'
      })
      .otherwise({
        redirectTo: function() {
          return '/edit/';
        }
      });
  });
