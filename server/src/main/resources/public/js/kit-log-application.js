var kitLogApplication = angular.module('KitLogApp', ['ngCookies', 'http-auth-interceptor']);
kitLogApplication.config(['$httpProvider', '$provide', function ($httpProvider, $provide) {

    $provide.factory('faultHttpInterceptor', function($q, $rootScope) {
      return {
        responseError: function(rejection) {
          var error = {};
          if (rejection.status === 0) {
            error.message = 'Problemer med serverkommunikasjon!';
          } else {
            error.message = rejection.data;
            error.httpStatus = { id: rejection.status, text: rejection.statusText };
          }
          $rootScope.$broadcast('generalHttpError', error);
          return $q.reject(rejection);
        }
      };
    });
    $httpProvider.interceptors.push('faultHttpInterceptor');
  }]);
