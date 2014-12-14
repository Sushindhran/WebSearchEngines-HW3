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

    delete $http.defaults.headers.common['X-Requested-With']
    var getData = function(prefix, callbackFunc) {
        $http({
            method: 'GET',
            url: 'http://localhost:25810/suggest?prefix='+prefix
        }).success(function(data){
            // With the data succesfully returned, call our callback
            callbackFunc(data);
        }).error(function(){
            alert("error");
        });
    };

    QueryRetriever.getSuggestions = function(query) {
        var queryData = $q.defer();

        var queries = ["Obama", "Web Search Engines", "New York", "Rasika", "Sushi", "Liverpool", "Cambridge University", "Soccer"];

        getData(query, function(data) {
            console.log("JSON "+data);
            queries = JSON.parse(data);
        });

        $timeout(function(){
            queryData.resolve(queries);
        },1000);

        return queryData.promise
    };
    return QueryRetriever;
});
