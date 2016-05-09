/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

define(
    [
        'angular',
        'angular-route',
        'angular-animate',
        'angular-sanitize'
    ],

function(angular) {

  var repoConnectionApp = angular.module("repo-connection-app", [
    'ngRoute',
    'ngAnimate',
    'ngSanitize',
    'repoConnectionAppControllers'
  ]);

  repoConnectionApp.config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.
      when('/pentaho-repository', {
        templateUrl: 'pentaho-repository.html',
        controller: 'PentahoRepositoryController'
      }).
      when('/create-new-connection', {
        templateUrl: 'create-new-connection.html',
        controller: 'CreateNewConnectionController'
      }).
      when('/pentaho-repository-connection-details', {
        templateUrl: 'pentaho-repository-connection-details.html',
        controller: 'PentahoRepositoryController'
      }).
      when('/pentaho-repository-creation-success', {
        templateUrl: 'creation-success.html',
        controller: 'PentahoRepositoryCreationSuccessController'
      }).
      when('/pentaho-repository-creation-failure', {
        templateUrl: 'creation-failure.html',
        controller: 'PentahoRepositoryCreationFailureController'
      }).
      when('/pentaho-repository-connect', {
        templateUrl: 'pentaho-repository-connect.html',
        controller: 'PentahoRepositoryConnectController'
      }).
      when('/kettle-file-repository-details', {
        templateUrl: 'kettle-file-repository-details.html',
        controller: 'KettleFileRepositoryDetailsController'
      }).
      when('/kettle-file-repository-creation-success', {
        templateUrl: 'creation-success.html',
        controller: 'KettleFileRepositoryCreationSuccessController'
      }).
      when('/kettle-file-repository-creation-failure', {
        templateUrl: 'creation-failure.html',
        controller: 'KettleFileRepositoryCreationFailureController'
      }).
      when('/kettle-database-repository-details', {
        templateUrl: 'kettle-database-repository-details.html',
        controller: 'KettleDatabaseRepositoryDetailsController'
      }).
      when('/kettle-database-repository-select', {
        templateUrl: 'kettle-database-repository-select.html',
        controller: 'KettleDatabaseRepositorySelectController'
      }).
      when('/kettle-database-repository-creation-success', {
        templateUrl: 'creation-success.html',
        controller: 'KettleDatabaseRepositoryCreationSuccessController'
      }).
      when('/kettle-database-repository-creation-failure', {
        templateUrl: 'creation-failure.html',
        controller: 'KettleDatabaseRepositoryCreationFailureController'
      }).
      when('/kettle-database-connection', {
        templateUrl: 'pentaho-repository-connect.html',
        controller: 'KettleDatabaseRepositoryConnectController'
      }).
      when('/repository-manager', {
        templateUrl: 'repository-manager.html',
        controller: 'RepositoryManagerController'
      }).
      otherwise({
        redirectTo: '/pentaho-repository'
      });
    }]).
    run(function($rootScope, $window) {
    $rootScope.slide = '';
    $rootScope.$on('$routeChangeStart', function() {
        $rootScope.back = function() {
            $rootScope.slide = 'to-right';
            $window.history.back();
        }
        $rootScope.next = function() {
            $rootScope.slide = 'to-left';
        }
        $rootScope.backFade = function() {
            $rootScope.slide = 'back-fade';
            $window.history.back();
        }
        $rootScope.nextFade = function() {
            $rootScope.slide = 'next-fade';
        }
      })
    });

  repoConnectionApp.animation('.to-left', [function() {
    return {
      enter: function(element, doneFn) {
        $(element).css("left", $(window).width())
        $(element).animate({
          left: 0
        });
      },
      leave: function(element, doneFn) {
        $(element).animate({
          left: -$(window).width()
        })
      }
    }
  }]);

  repoConnectionApp.animation('.to-right', [function() {
    return {
      enter: function(element, doneFn) {
        $(element).css("left", -$(window).width())
        $(element).animate({
          left: 0
        });
      },
      leave: function(element, doneFn) {
        $(element).animate({
          left: $(window).width()
        })
      }
    }
  }]);

  repoConnectionApp.animation('.back-fade', [function() {
    return {
      enter: function(element, doneFn) {
        $(element).css("opacity", 0)
        $(element).animate({
          opacity: 1
        }, 600);
      },
      leave: function(element, doneFn) {
        $(element).animate({
          opacity: 0
        }, 600);
      }
    }
  }]);

  repoConnectionApp.animation('.next-fade', [function() {
    return {
      enter: function(element, doneFn) {
        $(element).css("opacity", 0)
        $(element).animate({
          opacity: 1
        }, 600);
      },
      leave: function(element, doneFn) {
        $(element).animate({
          opacity: 0
        }, 600)
      }
    }
  }]);

  return repoConnectionApp;

});
