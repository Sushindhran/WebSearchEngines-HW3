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

            //Routes to game.html for /search
            $routeProvider.when('/search', {
                templateUrl: '/WebSearchEngines/src/main/application/view/search.html'
            });

            //Defaults to game.html if it doesn't match any route
            $routeProvider.otherwise({ templateUrl: '/WebSearchEngines/src/main/application/view/search.html' });
        }
    ]
);
