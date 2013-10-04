function ScanController($scope, $http) {
    function populateItems() {
        $http.get('rest/products/items').success(function(data) {
            $scope.items = data;
        })
    }

    var code = getParameterByName('code');
    if (code) {
        $http.get('rest/products?code=' + code).success(function(data) {
            if (data.length > 1) {
                $scope.products = data;
            } else if (data.length == 0) {
                $scope.newProduct = { code: code };
            } else {
                $http.put('rest/products/items', {'code': code, 'productId': data[0].id}).success(function(data) {
                    populateItems();
                })
            }
        });
    } else {
        populateItems();
    }

    $scope.saveProduct = function(product) {
        $http.put('rest/products', product).success(function(data) {
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
