WebSearchEngines.Controllers.controller('view.ResultsController',
    ['$scope', 'services.SearchService',
        function ($scope, searchService) {
            $scope.results = searchService.getResults();
        }
    ]);