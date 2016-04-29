/*
 * hook everything up and boostrap app.js on page
 */
define(
    [
      'angular',
      'angular-sanitize',
      'angular-route',
      'angular-animate',
      'repositories',
      'repositories/models',
      'repositories/controllers'
    ],

    function (angular) {
        'use strict';

        return {
            name: 'repositoriesMain',
            init: function(doc) {
              angular.element(doc).ready(function () {
                  angular.bootstrap(doc, ['repo-connection-app']);
              });
            }
        };
    }
);
