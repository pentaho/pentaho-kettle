
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

      move: function(element, doneFn) {
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

  repoConnectionApp.directive('buttonSelect', function() {
      return function($scope, element) {
        element.bind('click', function() {
          var left = $(element).offset().left + $(element).outerWidth() / 2;
          $('#triangle-up').animate({
            left: left - $('#triangle-up').outerWidth() / 2
          }, 250, "linear");
        });
      }
  });

  repoConnectionApp.directive('onFinishRender', function ($timeout) {
      return {
          restrict: 'A',
          link: function (scope, element, attr) {
              if (scope.$last === true) {
                  $timeout(function () {
                      scope.$emit('ngRepeatFinished');
                  });
              }
          }
      }
  });

  repoConnectionApp.directive('eventFocus', function(focus) {
    return function(scope, elem, attr) {
      elem.on(attr.eventFocus, function() {
        focus(attr.eventFocusId);
      });

      // Removes bound events in the element itself
      // when the scope is destroyed
      scope.$on('$destroy', function() {
        elem.off(attr.eventFocus);
      });
    };
  });
  return repoConnectionApp;

});
