/*!
 * Copyright 2019 Hitachi Vantara. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

define([
  "angular"
], function(angular) {
  /**
   * @param {Function} $timeout - Angular wrapper for window.setTimeout.
   * @return {{restrict: string, scope: {onStart: string, onComplete: string, onCancel: string, new: string,
   * value: string, auto: string, editing: string}, template: string, link: link}} - Edit directive
   */
  function edit($timeout, $document) {
    return {
      restrict: "AE",
      scope: {
        onStart: "&",
        onComplete: "&",
        onCancel: "&",
        new: "<",
        value: "<",
        auto: "=",
        editing: "=",
        count: "<"
      },
      template: '<span ng-mousedown="mousedown($event)" ng-click="edit($event)" ng-mousemove="mousemove($event)" ng-bind="updated"></span><input ng-model="updated"/>',
      link: function(scope, element, attr) {
        var inputElement = element.children()[1];
        var canEdit = false;
        var willEdit = false;
        var moved = false;
        var promise;
        scope.updated = scope.value;
        if (scope.auto) {
          edit();
        }
        scope.$watch("value", function(value) {
          scope.updated = value;
        });
        scope.$watch("editing", function(editing) {
          if (editing) {
            edit();
          }
        });
        scope.mousedown = function(e) {
          moved = false;
        };
        scope.mousemove = function(e) {
          moved = true;
        };
        scope.edit = function(e) {
          if (e.shiftKey || e.ctrlKey || e.metaKey || scope.count !== 1 || moved) {
            return;
          }
          if (willEdit) {
            $timeout.cancel(promise);
            willEdit = false;
            return;
          }
          var isSelected = element.parent().parent().parent().hasClass("selected");
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

        /**
         * Edit function
         */
        function edit() {
          scope.editing = true;
          scope.auto = false;
          scope.onStart();
          willEdit = false;
          canEdit = true;
          $timeout(function() {
            element.addClass("editing");
            $timeout(function() {
              inputElement.focus();
              inputElement.select();
            });
          });
        }

        var mouseDown = false;

        $document.on("mousedown", function(e) {
          mouseDown = false;
        });

        angular.element(inputElement).on("mousedown", function(e) {
          mouseDown = true;
          e.stopPropagation();
        });

        angular.element(inputElement).on("keyup blur", function(e) {
          if (e.type == "blur" && mouseDown) {
            return;
          }
          if (e.keyCode === 13 || e.keyCode === 27 || e.type === "blur") {
            if (e.keyCode === 27) {
              scope.updated = "";
            }
            finish();
          }
          e.stopPropagation();
        });

        /**
         * Called when editing is finished
         */
        function finish() {
          scope.editing = false;
          if (!element.hasClass("editing")) {
            return;
          }
          element.removeClass("editing");
          inputElement.blur();
          if (scope.updated === "") {
            scope.updated = scope.value;
          }
          if (scope.new || scope.updated !== scope.value) {
            scope.onComplete({current: scope.updated, previous: scope.value});
          } else {
            scope.onComplete(null);
          }
          scope.$apply();
        }
      }
    };
  }

  return {
    name: "edit",
    options: ["$timeout", "$document", edit]
  };
});
