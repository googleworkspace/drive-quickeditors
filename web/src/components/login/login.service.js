
/**
 * Service wrapper for gapi auth functions
 */
angular.module('editor.login').service('login', ['$q', '$mdDialog', 'googleApi', 'clientId', 'scope', function ($q, $mdDialog, googleApi, clientId, scope) {

  /**
  * Check if the current token is valid (exists & not expired.)
  *
  * @return {Boolean} True if token still valid (not expired)
  */
  var isTokenValid = function() {
    var token = gapi.auth.getToken();
    return (token && Date.now() < token.expires_at);
  };

  /**
   * Builds a request object suitable for gapi.auth.authorize calls.
   *
   * @param {Boolean} immediateMode True if auth should be checked silently
   * @param {String} user Optional login hint indiciating which account should be authorized
   * @return {Promise} promise that resolves on completion of the login
   */
  var buildAuthRequest = function(immediateMode, user) {
    var request = {
      client_id: clientId,
      scope: scope,
      immediate: immediateMode
    };
    if (user) {
      request.login_hint = user;
      request.authuser = -1;
    }
    return request;
  };

  /**
   * Attempt authorization.
   *
   * @param {Object} request Auth request
   * @return {Promise} promise that resolves on completion
   */
  var executeRequest = function(request) {
    return googleApi.then(function(gapi) {
      if (isTokenValid()) {
        return gapi.auth.getToken();
      } else {
        var deferred = $q.defer();
        gapi.auth.authorize(request, function(result) {
          if (result && !result.error) {
            deferred.resolve(result);
          } else {
            var error = result ? result.error : 'Unknown authentication error';
            deferred.reject(error);
          }
        });
        return deferred.promise;
      }
    });
  };

  /**
   * Prompt user for login/authorization
   *
   * @param {String} user Optional login hint indiciating which account should be authorized
   * @return {Promise} promise that resolves on completion of the login
   */
  this.login = function (user) {
    var request = buildAuthRequest(false, user);
    return executeRequest(request);
  };

  /**
   * Silently check to see if a user has already authorized the app.
   *
   * @param {String} user Optional login hint indiciating which account should be authorized
   * @return {Promise} promise that resolves on completion of the check
   */
  this.checkAuth = function(user) {
    var request = buildAuthRequest(true, user);
    return executeRequest(request);
  };

  /**
   * Displays a dialog with a login button.
   *
   * @param {Event} $event Optional click event for animations
   * @param {String} user Optional user ID hint if a particular account is required
   */
  this.showLoginDialog = function($event, user) {
    return $mdDialog.show({
      targetEvent: $event,
      templateUrl: "components/login/login.html",
      controller: 'LoginCtrl',
      clickOutsideToClose: false,
      escapeToClose: false,
      controllerAs: 'ctrl',
      locals: {
        user: user
      }
    });
  };
}]);
