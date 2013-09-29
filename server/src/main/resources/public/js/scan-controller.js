function ScanController($scope, $http) {
    var code = getParameterByName('code');
    $http.get('rest/product?code=' + code).success(function(data) {
        console.log("Length: " + data.length)
        if (data.length > 1) {
            $scope.products = data;
        } else if (data.length == 0) {
            $scope.newProduct = { code: code };
        } else {
            $http.put('rest/product/item', {'code': code, 'productId': data[0].id}).success(function(data) {

            })
        }
    });

    $scope.saveProduct = function(product) {
        $http.put('rest/product', product).success(function(data) {
            alert("Da sa han: " + data);
        });
    };

    /*$scope.products = [
        {"name": "Nexus S",
         "code": "5423"},
        {"name": "Motorola XOOM™ with Wi-Fi",
         "code": "43123"},
        {"name": "MOTOROLA XOOM™",
         "code": "43728432"}
      ];*/
}
