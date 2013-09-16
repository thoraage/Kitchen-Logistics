function ScanController($scope, $http) {
//    $http.defaults.headers.put['Content-Type'] = 'application/json';
    $http.get('rest/product?' + getParameterByName('code')).success(function(data) {
        $scope.products = data;
        //$('#newProductForm').removeClass('hide');
    });
    /*$scope.products = [
        {"name": "Nexus S",
         "code": "5423"},
        {"name": "Motorola XOOM™ with Wi-Fi",
         "code": "43123"},
        {"name": "MOTOROLA XOOM™",
         "code": "43728432"}
      ];*/
}
