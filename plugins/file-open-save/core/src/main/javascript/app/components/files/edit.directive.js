define([
  'angular'
], function (angular) {

  edit.$inject = ['$timeout'];

  function edit($timeout) {
    return {
      retrict: 'AE',
      scope: {
        onStart: '&',
        onComplete: '&',
        onCancel: '&',
        new: '<',
        value: '<',
        auto: '=',
        editing: '='
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
          if (!isSelected) {
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
          scope.editing = true;
          scope.auto = false;
          scope.onStart();
          willEdit = false;
          canEdit = true;
          $timeout(function() {
            element.addClass('editing');
            editing = true;
            $timeout(function() {
              inputElement.focus();
              inputElement.select();
            });
          });
        }

        angular.element(inputElement).on('keydown blur', function(e) {
          if (e.keyCode === 13 || e.keyCode === 27 || e.type === "blur") {
            if (e.keyCode === 27) {
              scope.updated = "";
            }
            finish();
          }
        });

        function finish() {
          scope.editing = false;
          if (!element.hasClass('editing')) {
            return;
          }
          if (scope.updated === "") {
            scope.updated = scope.value;
          }
          if (scope.new || scope.updated !== scope.value) {
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
    options: ['$timeout', edit]
  }
});
