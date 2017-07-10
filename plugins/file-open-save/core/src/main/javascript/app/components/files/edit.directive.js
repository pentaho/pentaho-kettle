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
        value: '=',
        auto: '='
      },
      template: '<span ng-click="edit()" ng-bind="value"></span><input ng-model="value"/>',
      link: function (scope, element, attr) {
        var inputElement = element.children()[1];
        var canEdit = false;
        var willEdit = false;
        var promise;
        var previous;
        if (scope.auto) {
          edit();
        }
        scope.edit = function() {
          previous = scope.value;
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

          if (canEdit) {
            willEdit = true;
            promise = $timeout(edit, 200);
          }
        };

        function edit() {
          scope.onStart();
          willEdit = false;
          element.addClass('editing');
          inputElement.focus();
          setTimeout(function() {
            inputElement.select();
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
          if (previous !== scope.value) {
            scope.onComplete({'name' : previous});
          }
          element.removeClass('editing');
        }

        if (scope.auto) {
          scope.edit();
        }
      }
    }
  }

  return {
    name: "edit",
    options: ['$compile', '$timeout', edit]
  }
});
