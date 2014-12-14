WebSearchEngines.Services.factory('services.SearchService', function($rootScope, $http, $q, $timeout){
    var QueryRetriever = {};
    var results;

    QueryRetriever.setResults = function(res) {
        results = res;
    };

    QueryRetriever.getResults = function() {
        console.log('In get '+results);
        return results;
    };

    delete $http.defaults.headers.common['X-Requested-With'];
    var getData = function(prefix, callbackFunc) {
        $http({
            method: 'GET',
            url: 'http://localhost:25810/suggest?prefix='+prefix
        }).success(function(data) {
            // With the data succesfully returned, call our callback
            callbackFunc(data);
        }).error(function(data, status, headers, config) {
            // called asynchronously if an error occurs
            // or server returns response with an error status.
            console.log(data, status, headers, config);
        });
    };

    QueryRetriever.getSuggestions = function(query) {
        var queryData = $q.defer(),
            queries = [];

        if(query !== "") {
            getData(query, function(data) {
                queries = data;
            });
        }

        $timeout(function(){
            queryData.resolve(queries);
        },1000);

        return queryData.promise
    };

    return QueryRetriever;
});
