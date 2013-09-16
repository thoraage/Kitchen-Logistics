function ScanController($scope) {
    $http.get('rest/product?' + getParameterByName('code')).success(function(data) {
        $scope.product = data;
    });
    $('#newProductForm').removeClass('hide');
}
