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

        var queries = ["Obama", "The Wolverine", "The Smurfs 2", "The Mortal Instruments: City of Bones", "Drinking Buddies", "All the Boys Love Mandy Lane", "The Act Of Killing", "Red 2", "Jobs", "Getaway", "Red Obsession", "2 Guns", "The World's End", "Planes", "Paranoia", "The To Do List", "Man of Steel"];

        $timeout(function(){
            queryData.resolve(queries);
        },1000);

        return queryData.promise
    };
    return QueryRetriever;
});
