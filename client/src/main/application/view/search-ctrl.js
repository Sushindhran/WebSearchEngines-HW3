//Game controller that contains some of the game logic and UI
WebSearchEngines.Controllers.controller('view.SearchController',
    ['$scope', 'services.SearchService',
        function ($scope, searchService) {
            $scope.suggestions = searchService.getSuggestions("...");
            $scope.suggestions.then(function(data){
                $scope.suggestions = data;
            });

            $scope.getmovies = function(){
                return $scope.suggestions;
            }

            $scope.doSomething = function(typedthings){
                console.log("Do something like reload data with this: " + typedthings );
                $scope.queries = searchService.getSuggestions(typedthings);
                $scope.queries.then(function(data){
                    $scope.suggestions = data;
                });
            }

            $scope.doSomethingElse = function(suggestion){
                console.log("Suggestion selected: " + suggestion );
            }
        }
    ]);