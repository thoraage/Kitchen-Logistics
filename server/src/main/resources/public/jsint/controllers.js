function ProductCtrl($scope, $http) {
  $scope.product = {
    "code": getParameterByName('identifyScanCode')
  };

  $scope.update = function(product) {
    $http.put("/rest/product", product)
    .success(function(data, status, headers, config) {
        $('#scanCodeIdentification').addClass('hidden');
    }).error(function(data, status, headers, config) {
        alert("DOH: " + data + ", " + status + ", " + headers + ", " + config);
    });
  };
}

function PhoneListCtrl($scope) {
  $scope.phones = [
    {"name": "Nexus S ",
     "snippet": "Fast just got faster with Nexus S."},
    {"name": "Motorola XOOM™ with Wi-Fi",
     "snippet": "The Next, Next Generation tablet."},
    {"name": "MOTOROLA XOOM™",
     "snippet": "The Next, Next Generation tablet."}
  ];
}

