//Game controller that contains some of the game logic and UI
WebSearchEngines.Controllers.controller('view.SearchController',
    ['$scope', '$http', 'services.SearchService',
        function ($scope, $http, searchService) {
            $scope.suggestions = searchService.getSuggestions("...");
            $scope.suggestions.then(function(data){
                $scope.suggestions = data;
            });

            $scope.doSomething = function(typedthings){
                console.log("Do something like reload data with this: " + typedthings );
                $scope.queries = searchService.getSuggestions(typedthings);
                $scope.queries.then(function(data){
                    $scope.suggestions = data;
                });
            };

            $scope.doSomethingElse = function(suggestion){
                console.log("Suggestion selected: " + suggestion);
                $scope.getData(function(data) {
                    console.log(data);
                });
            };

            delete $http.defaults.headers.common['X-Requested-With'];
            $scope.getData = function(callbackFunc) {
                $http({
                    method: 'GET',
                    url: 'http://localhost:25810/search?query='+$scope.suggestions+'&ranker=comprehensive&format=text'
                }).success(function(data){
                    // With the data succesfully returned, call our callback
                    callbackFunc(data);
                }).error(function(){
                    alert("error");
                });
            };
        }
    ]);