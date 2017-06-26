/*!
 * Copyright 2017 Pentaho Corporation. All rights reserved.
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
      'angular'
    ], function (angular) {
      function renameDirective($timeout) {

        return {
          restrict: 'AE',
          scope: {
            onRename: '&',
            selected: '<',
            ngModel: '='
          },
          link: function (scope, element, attr) {
            if (scope.ngModel.autoEdit) {
              scope.ngModel.autoEdit = false;
              edit();
              return;
            }

            var canEdit = false;
            var willEdit = false;
            var promise;
            element.on('click', function (e) {
              if (willEdit) {
                $timeout.cancel(promise);
                willEdit = false;
                return;
              }
              if (scope.selected !== scope.ngModel) {
                $timeout(function () {
                  canEdit = true;
                }, 200);
                canEdit = false;
                return;
              }

              if (canEdit) {
                willEdit = true;
                promise = $timeout(function () {
                  edit();
                }, 200);
              }
            });

            function edit() {
              element.parent().addClass("editing");
              willEdit = false;
              element.empty();
              var input = angular.element('<input/>');
              input.val(scope.ngModel.name);
              element.append(input);
              input.select();
              input.bind('keydown', function (e) {
                if (e.keyCode === 13) {
                  var value = input.val();
                  if (value !== '' && value !== scope.ngModel.name) {
                    scope.ngModel.name = value;
                    scope.$apply('onRename()');
                    finish();
                  } else {
                    finish();
                  }
                }
                if (e.keyCode === 27) {
                  finish();
                }
              });
              input.bind('blur', function (e) {
                finish();
              });
            }

            function finish() {
              element.parent().removeClass("editing");
              element.html(scope.ngModel.name);
            }
          }
        };
      }

      return {
        name: "rename",
        options: ['$timeout', renameDirective]
      };
    }
);
