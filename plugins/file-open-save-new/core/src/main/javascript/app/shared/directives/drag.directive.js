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
  'angular'
], function (angular) {
  /**
   * @param {Function} $rootScope - Angular rootScope for setting data that can be passed around.
   * @return {{restrict: string, scope: {model: string}, link: link}} - Drag directive
   */
  function drag($rootScope) {
    return {
      restrict: 'A',
      scope: {
        model: '<ngModel',
        file: '<'
      },
      link: function (scope, element, attrs) {
        var track = false;
        element.bind("mousedown", function (e) {
          var startX = e.clientX;
          var startY = e.clientY;
          var index = scope.model.indexOf(scope.file);
          var content;
          if (index === -1 || (scope.model.length === 1 && scope.model[0] === scope.file)) {
            content = scope.file.name;
          } else {
            content = scope.model.length + " files";
          }
          var item = angular.element("<div class=\"drag-file\">" + content + "</div>");
          angular.element(document.body).bind("mousemove", function (e) {
            if (!track) {
              var deltaX = Math.abs(e.clientX - startX);
              var deltaY = Math.abs(e.clientY - startY);
              if (deltaX > 5 || deltaY > 5) {
                track = true;
                var index = scope.model.indexOf(scope.file);
                if (index === -1) {
                  $rootScope.dragData = [scope.file];
                } else {
                  $rootScope.dragData = scope.model;
                }
                angular.element(document.body).append(item);
                item.css({
                  position: "absolute",
                  left: e.clientX + 5,
                  top: e.clientY + 5
                });
                angular.element(document.body).bind("keydown", function (e) {
                  if (e.keyCode === 27) {
                    cleanup();
                  }
                });
              }
            } else {
              item.css({
                left: e.clientX + 5,
                top: e.clientY + 5
              });
            }
          });
          angular.element(document.body).bind("mouseup", function () {
            cleanup();
          });

          function cleanup() {
            track = false;
            item.remove();
            angular.element(document.body).unbind("mousemove");
            angular.element(document.body).unbind("keydown");
            $rootScope.dragData = null;
          }
        });
      }
    };
  }

  return {
    name: "drag",
    options: ['$timeout', drag]
  };
});
