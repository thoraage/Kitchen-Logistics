function ScanController($scope, $http) {
    function populateItems() {
        $http.get('rest/products/items').success(function(data) {
            $scope.items = data;
        })
    }

    function putItemAndPopulate(id) {
        $http.put('rest/products/items', {'productId': id}).success(function(data) {
            populateItems();
        })
    }

    var code = getParameterByName('code');
    if (code && getParameterByName('add')) {
        $http.get('rest/products?code=' + code).success(function(data) {
            if (data.length > 1) {
                $scope.products = data;
            } else if (data.length == 0) {
                $scope.newProduct = { code: code };
            } else {
                putItemAndPopulate(data[0].id);
            }
        });
    } else if (code && getParameterByName('remove')) {
        alert("doh");
    } else {
        populateItems();
    }

    $scope.saveProduct = function(product) {
        $http.put('rest/products', product).success(function(data) {
            $scope.newProduct = null;
            putItemAndPopulate(data.id);
        });
    };

    $scope.removeItem = function(itemId) {
       alert("item id: " + itemId);
   }

}
