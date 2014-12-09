WebSearchEngines.Controllers.controller('view.SearchController',
    ['$scope', '$http', '$location', 'services.SearchService',
        function ($scope, $http, $location, searchService) {
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
                    alert("error");
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