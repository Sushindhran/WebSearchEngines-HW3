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

    QueryRetriever.getSuggestions = function(query) {
        var queryData = $q.defer();

        var queries = ["Obama", "Web Search Engines", "New York", "Rasika", "Sushi", "Liverpool", "Cambridge University", "Soccer"];

        $timeout(function(){
            queryData.resolve(queries);
        },1000);

        return queryData.promise
    };
    return QueryRetriever;
});
