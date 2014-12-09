/** This file defines all the dependencies that the AngularJS application will use
 *  like filters, services, directives and controllers.
 */

var WebSearchEngines = WebSearchEngines || {};

WebSearchEngines.Constants = angular.module('websearchengines.constants', []);
WebSearchEngines.Services = angular.module('websearchengines.services', []);
WebSearchEngines.Controllers = angular.module('websearchengines.controllers', []);
WebSearchEngines.Filters = angular.module('websearchengines.filters', []);
WebSearchEngines.Directives = angular.module('websearchengines.directives', []);
WebSearchEngines.Dependencies = [
    'websearchengines.filters',
    'websearchengines.services',
    'websearchengines.directives',
    'websearchengines.constants',
    'websearchengines.controllers',
    'ngRoute'
];

//Define the routes for the application using $routeProvider
angular.module(
    'websearchengines',
    WebSearchEngines.Dependencies
).
    config(
    [
        '$routeProvider',
        function($routeProvider) {
            //Routes to search.html for /search
            $routeProvider.when('/search', {
                templateUrl: 'application/view/search.html'
            });

            $routeProvider.when('/results', {
                templateUrl: 'application/view/results.html'
            });

            //Defaults to search.html if it doesn't match any route
            $routeProvider.otherwise({ templateUrl: '/client/src/main/application/view/search.html' });
        }
    ]
);
