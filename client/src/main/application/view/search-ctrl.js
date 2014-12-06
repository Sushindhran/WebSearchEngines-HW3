//Game controller that contains some of the game logic and UI
TicTacToe.Controllers.controller('view.SearchController',
    ['$scope', 'constants.Configuration', 'services.SearchService',
        function ($scope, configuration, searchService) {
            $scope.suggestions = searchService.getmovies("...");
            $scope.suggestions.then(function(data){
                $scope.suggestions = data;
            });

            $scope.getmovies = function(){
                return $scope.suggestions;
            }

            $scope.doSomething = function(typedthings){
                console.log("Do something like reload data with this: " + typedthings );
                $scope.newmovies = searchService.getmovies(typedthings);
                $scope.newmovies.then(function(data){
                    $scope.suggestions = data;
                });
            }

            $scope.doSomethingElse = function(suggestion){
                console.log("Suggestion selected: " + suggestion );
            }
        }
    ]);