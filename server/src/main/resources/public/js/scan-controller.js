var kitLogApp = angular.module('KitLogApp', ['ngCookies']);

kitLogApp.controller('ScanController', function ScanController($scope, $http, $cookies, $log, $location, $window) {
    function populateItems() {
        $http.get('rest/items').success(function(data) {
            $scope.items = data;
        });
    }

    function putItemAndPopulate(id) {
        $http.put('rest/items', {'productId': id}).success(function(data) {
            populateItems();
        });
    }

    function loadItemGroups() {
        $http.get('rest/itemGroups').success(function(data) {
            var itemGroupId = $cookies.selectedItemGroupId;
            if (itemGroupId) {
                itemGroupId = parseInt(itemGroupId);
            }
            data.push({id: -1, name: 'New item group'});
            $scope.itemGroups = data;
            for (i = 0; i < $scope.itemGroups.length; ++i) {
                var itemGroup = $scope.itemGroups[i];
                if (itemGroup.id == itemGroupId) {
                    $scope.itemGroup = itemGroup;
                }
            }
        });
    }

    loadItemGroups();

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
       $http.delete('rest/items/' + itemId).success(function() {
            populateItems();
       });
    };

    $scope.changeItemGroup = function() {
        if ($scope.itemGroup) {
            if ($scope.itemGroup.id == -1) {
                $scope.editItemGroup = {};
                $scope.itemGroup = null;
                $cookies.selectedItemGroupId = null;
            } else {
                $cookies.selectedItemGroupId = $scope.itemGroup.id.toString();
            }
        }
    };


    $scope.saveItemGroup = function(newItemGroup) {
        $http.put('rest/itemGroups', newItemGroup).success(function(id) {
            $cookies.selectedItemGroupId = id;
            loadItemGroups();
        });
    }

    function createScanUrl(add, multiple) {
        var ret = encodeURIComponent($location.absUrl().replace(/\?.*/, '') + '?code={CODE}&' + (add?'add':'remove') + '=1' + (multiple?'&repeat':''));
        $window.location.href = 'zxing://scan/?ret=' + ret;
    }

    $scope.scanNew = function(multiple) { createScanUrl(true, multiple); }
    $scope.scanRemove = function(multiple) { createScanUrl(false, multiple); }

});
