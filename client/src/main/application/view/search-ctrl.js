WebSearchEngines.Controllers.controller('view.SearchController',
    ['$scope', '$http', '$location', '$window', 'services.SearchService',
        function ($scope, $http, $location, $window, searchService) {
            $scope.suggestions = searchService.getSuggestions('');

            var getLocation = function(pos) {
                var geocoder = new google.maps.Geocoder();
                var latlng = new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
                geocoder.geocode({ 'latLng': latlng }, function (results, status) {
                    if (status == google.maps.GeocoderStatus.OK) {
                        if (results[1]) {
                            $scope.$apply(function() {
                               $scope.location = results[1].formatted_address;
                               var location = {
                                   locality: results[1].address_components[0].long_name,
                                   city: results[1].address_components[1].long_name,
                                   county: results[1].address_components[2].long_name,
                                   state: results[1].address_components[3].long_name,
                                   country: results[1].address_components[4].long_name
                               };

                               setLocation(function(data) {
                                  console.log("Location set successfully");
                               });

                            });
                        } else {
                            console.log('Location not found');
                        }
                    } else {
                        console.log('Geocoder failed due to: ' + status);
                    }
                });
            };

            var getGeoLocation = function() {
                $window.navigator.geolocation.getCurrentPosition(function(pos) {
                    getLocation(pos);
                }, function(error) {
                    alert(error);
                });
            };

            getGeoLocation();

            $scope.suggestions.then(function(data) {
                $scope.suggestions = data;
            });

            $scope.doSomething = function(typedthings) {
                console.log("Do something like reload data with this: " + typedthings);
                $scope.queries = searchService.getSuggestions(typedthings);
                $scope.queries.then(function(data){
                    $scope.suggestions = data;
                });
            };

            $scope.doSomethingElse = function(suggestion) {
                console.log("Suggestion selected: " + suggestion);
                $scope.query = suggestion;
                $scope.getData(function(data) {
                    var pData = processString(data);
                    searchService.setResults(pData);
                    $location.path('/results');
                });
            };

            delete $http.defaults.headers.common['X-Requested-With'];
            $scope.getData = function(callbackFunc) {
                $http({
                    method: 'GET',
                    url: 'http://localhost:25810/search?query='+$scope.query+'&ranker=comprehensive&format=text'
                }).success(function(data){
                    // With the data succesfully returned, call our callback
                    callbackFunc(data);
                }).error(function(){
                    console.log("Error in query");
                });
            };

            var setLocation = function(callbackFunc) {
                $http({
                    method: 'GET',
                    url: 'http://localhost:25810/location?location='+$scope.location
                }).success(function(data){
                    // With the data succesfully returned, call our callback
                    callbackFunc(data);
                }).error(function(){
                    console.log("Error in setting location");
                });
            };

            var processString = function(data) {
                var pData = [],
                    arr = data.split('\n');

                for(var i = 0; i < arr.length; i++) {
                    var line = {},
                        lineArr = arr[i].split("\t");
                    line.docId = lineArr[0];
                    line.title = lineArr[1];
                    line.score = lineArr[2];
                    line.pageRank = lineArr[3];
                    line.numdocs = lineArr[4];
                    line.url = 'url?did='+line.docId;
                    pData.push(line);
                }
                return pData;
            }
        }
    ]);