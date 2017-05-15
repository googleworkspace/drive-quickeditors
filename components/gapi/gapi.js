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

/**
 * Callback indicating the gapi library is available & bridge into the angular world. Since
 * this may be called before angular initializes, it may poll temporarily until the 2nd-stage
 * 'init_gapi' function is available.
 */
function on_gapi_loaded() {
  if (window.init_gapi) {
    window.init_gapi();
  } else {
    setTimeout(on_gapi_loaded, 10);
  }
}

var module = angular.module('editor.gapi', []);

/**
 * Adapter for exposing gapi as an angular service. This registers a promise that will
 * resolve to gapi after all the APIs have been loaded.
 */
module.factory('googleApi', ['$rootScope', '$window', '$q', 'apiKey', 'loadApis', function($rootScope, $window, $q, apiKey, loadApis) {
  var googleApi = $q.defer();

  $window.init_gapi = function() {
    $rootScope.$apply(function() {
      var apis = [];
      if (apiKey) {
        $window.gapi.client.setApiKey(apiKey);
      }
      angular.forEach(loadApis, function(value, key) {
        apis.push($q.when(gapi.client.load(key, value)));
      });
      $q.all(apis).then(function() {
        googleApi.resolve($window.gapi);
      });
    });
  };

  return googleApi.promise;
}]);

