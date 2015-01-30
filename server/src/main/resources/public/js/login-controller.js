kitLogApplication.controller('LoginController', function LoginController($scope, $http, authService) {

    $scope.$on('event:auth-loginRequired', function() {
        $('#loginModal').modal('show');
    });
    $scope.$on('event:auth-loginConfirmed', function() {
        console.log("Logged in");
        $('#loginModal').modal('hide');
    });
    $scope.login = function() {
        $http({method: 'PUT', url: 'rest/authenticate', headers: {
            'Authorization': 'Basic ' + Base64.encode($scope.username + ':' + $scope.password)}
        }).success(function(data, status, headers, config) {
            authService.loginConfirmed();
        });
    }

});
