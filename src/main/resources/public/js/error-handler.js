'use strict';

kitLogApplication.controller('ErrorHandlerController', ['$scope', '$log', function($scope, $log) {

    $scope.$on('generalHttpError', function(event, data) {
      $log.debug(data);
      if (data.httpStatus) {
        if (data.httpStatus.id >= 500) {
          $scope.error = 'Ukjent teknisk feil: ' + data.httpStatus.text + ' (statuskode ' + data.httpStatus.id + ')';
          $scope.errorDetails = data.message;
        }
      } else {
        $scope.error = data.message;
      }
    });

  }]);
