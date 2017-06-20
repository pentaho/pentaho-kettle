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
        'angular-sanitize',
        'angular-translate',
        'repositories/angular-translate-static-loader'
    ],

function(angular) {

  var repoConnectionApp = angular.module("repo-connection-app", [
    'ngRoute',
    'ngAnimate',
    'ngSanitize',
    'repoConnectionAppControllers',
    'pascalprecht.translate'
  ]);

  repoConnectionApp.config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.
      when('/pentaho-repository', {
        templateUrl: 'pentaho-repository.html',
        controller: 'PentahoRepositoryController'
      }).
      when('/pentaho-repository-connection-details', {
        templateUrl: 'pentaho-repository-connection-details.html',
        controller: 'PentahoRepositoryController'
      }).
      when('/pentaho-repository-creation-success', {
        templateUrl: 'creation-success.html',
        controller: 'PentahoRepositoryController'
      }).
      when('/pentaho-repository-creation-failure', {
        templateUrl: 'creation-failure.html',
        controller: 'PentahoRepositoryController'
      }).
      when('/kettle-file-repository-details', {
        templateUrl: 'kettle-file-repository-details.html',
        controller: 'KettleFileRepositoryController'
      }).
      when('/kettle-file-repository-creation-success', {
        templateUrl: 'creation-success.html',
        controller: 'KettleFileRepositoryController'
      }).
      when('/kettle-file-repository-creation-failure', {
        templateUrl: 'creation-failure.html',
        controller: 'KettleFileRepositoryController'
      }).
      when('/kettle-database-repository-details', {
        templateUrl: 'kettle-database-repository-details.html',
        controller: 'KettleDatabaseRepositoryController'
      }).
      when('/kettle-database-repository-select', {
        templateUrl: 'kettle-database-repository-select.html',
        controller: 'KettleDatabaseRepositorySelectController'
      }).
      when('/kettle-database-repository-creation-success', {
        templateUrl: 'creation-success.html',
        controller: 'KettleDatabaseRepositoryController'
      }).
      when('/kettle-database-repository-creation-failure', {
        templateUrl: 'creation-failure.html',
        controller: 'KettleDatabaseRepositoryController'
      }).
      when('/create-new-connection', {
        templateUrl: 'create-new-connection.html',
        controller: 'CreateNewConnectionController'
      }).
      when('/repository-connect', {
        templateUrl: 'pentaho-repository-connect.html',
        controller: 'RepositoryConnectController'
      }).
      when('/loading-animation', {
        templateUrl: 'loading-animation.html',
        controller: 'LoadingAnimationController'
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
    $rootScope.fromEdit = false;
    $rootScope.slide = '';
    $rootScope.$on('$routeChangeStart', function() {
        $rootScope.back = function() {
            $rootScope.slide = 'to-right';
        }
        $rootScope.next = function() {
            $rootScope.slide = 'to-left';
        }
        $rootScope.backFade = function() {
            $rootScope.slide = 'back-fade';
        }
        $rootScope.nextFade = function() {
            $rootScope.slide = 'next-fade';
        }
      })
    });

  repoConnectionApp.config(['$translateProvider', function ($translateProvider) {

    $translateProvider.useStaticFilesLoader({
      prefix: 'lang/messages_',
      suffix: '.properties',
      fileFormat: 'properties'

    });
    // TODO: SESSION_LOCALE AS INJECTED VARIABLE INSTEAD OF GLOBAL
    var SESSION_LOCALE = 'en';
    $translateProvider.preferredLanguage(SESSION_LOCALE)
        .fallbackLanguage('en');

  }]);

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

  repoConnectionApp.directive('setFocus', ['$timeout', function($timeout) {
    return {
      restrict: 'A',
      link : function($scope, $element, $attr) {
        $timeout(function() {
          $element.focus();
          $element.val($element.val());
        }, 300);
      }
    };
  }]);

  repoConnectionApp.directive('errorMessage', [ '$timeout', function($timeout) {
    function link(scope, element, attrs) {
      var elem = element.find('.error-message');
      scope.dismiss = function() {
        $timeout.cancel(scope.timer);
        elem.animate({
          opacity : 0
        }, 600, function() {
          elem.css('top', -63);
        });
        scope.hasError = false;
      }
      scope.showError = function() {
        $timeout.cancel(scope.timer);
        elem.animate({
          top : 10,
          opacity : 1
        }, 600);
        scope.timer = $timeout(function() {
          scope.dismiss();
        }, 5000);
      }
      scope.triggerError = function(message) {
        scope.errorMessage = message;
        scope.hasError = true;
        scope.showError();
      }
      scope.resetErrorMsg = function() {
        $timeout.cancel(scope.timer);
        if (scope.hasError) {
          scope.dismiss();
          scope.hasError = false;
        }
      }
      scope.clearError = function() {
        $timeout.cancel(scope.timer);
        elem.stop();
        elem.css('top', -63);
        elem.css('opacity', 0);
        scope.errorMessage = "";
        scope.hasError = false;
      }
      scope.refreshError = function() {
        $timeout.cancel(scope.timer);
        elem.stop();
        elem.animate({
          opacity : 0
        }, 600, function() {
          scope.showError();
        });
      }
    }
    return {
      restrict : 'E',
      link : link,
      templateUrl : 'error-message.html'
    }
  } ]);

  return repoConnectionApp;

});
