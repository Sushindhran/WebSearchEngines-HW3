WebSearchEngines.Controllers.controller('view.ResultsController',
    ['$scope', 'services.SearchService',
        function ($scope, searchService) {
            $scope.filteredResults = [],
                $scope.currentPage = 1,
                $scope.numPerPage = 10,
                $scope.maxSize = 5;

            $scope.results = searchService.getResults();

            console.log($scope.results);


            $scope.getHtml = function(url) {
                console.log(url);
                window.location = 'http://localhost:25810/'+url
            };

            $scope.numPages = function () {
                return Math.ceil($scope.results.length / $scope.numPerPage);
            };

            $scope.$watch('currentPage + numPerPage', function() {
                var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                    , end = begin + $scope.numPerPage;

                $scope.filteredResults = $scope.results.slice(begin, end);
            });

        }
    ]);