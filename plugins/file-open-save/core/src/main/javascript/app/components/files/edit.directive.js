define([
  'angular'
], function (angular) {

  edit.$inject = ['$compile', '$timeout'];

  function edit($compile, $timeout) {
    return {
      retrict: 'AE',
      scope: {
        onStart: '&',
        onComplete: '&',
        value: '<',
        auto: '='
      },
      template: '<span ng-click="edit()" ng-bind="updated"></span><input ng-model="updated"/>',
      link: function (scope, element, attr) {
        var inputElement = element.children()[1];
        var canEdit = false;
        var willEdit = false;
        var promise;
        scope.updated = scope.value;
        if (scope.auto) {
          edit();
        }
        scope.edit = function() {
          if (willEdit) {
            $timeout.cancel(promise);
            willEdit = false;
            return;
          }
          var isSelected = element.parent().parent().parent().hasClass( "selected" );
          if ( !isSelected ) {
            $timeout(function() {
              canEdit = true;
            }, 200);
            canEdit = false;
            return;
          }

          if (canEdit || isSelected) {
            willEdit = true;
            promise = $timeout(edit, 200);
          }
        };

        function edit() {
          scope.auto = false;
          scope.onStart();
          willEdit = false;
          canEdit = true;
          $timeout(function() {
            element.addClass('editing');
            $timeout(function() {
              inputElement.focus();
              inputElement.select();
            });
          });
        }

        angular.element(inputElement).on('keydown', function(e) {
          if (e.keyCode === 13 || e.keyCode === 27) {
            finish();
          }
        });

        angular.element(inputElement).on('blur', function() {
          finish();
        });

        function finish() {
          if (!element.hasClass('editing')) {
            return;
          }
          if (scope.updated !== scope.value) {
            scope.onComplete({current: scope.updated, previous: scope.value, errorCallback: function() {
              scope.updated = scope.value;
            }});
          }
          element.removeClass('editing');
        }
      }
    }
  }

  return {
    name: "edit",
    options: ['$compile', '$timeout', edit]
  }
});
