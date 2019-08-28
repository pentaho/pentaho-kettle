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
   * @param {Function} $timeout - Angular wrapper for window.setTimeout.
   * @return {{restrict: string, scope: {model: string}, link: link}} - Focus directive
   */
  function drop($rootScope) {
    return {
      restrict: 'A',
      scope: {
        file: '<file',
        onDrop: '&'
      },
      link: function (scope, element, attrs) {
        element.bind('mouseenter', function() {
          element.addClass("over");
        });
        element.bind('mouseleave', function() {
          element.removeClass("over");
        });
        if (scope.file.type === "folder") {
          element.bind("mouseup", function (e) {
            var files = $rootScope.dragData;
            if (files) {
              if (files.indexOf(scope.file) === -1) {
                scope.onDrop({from:files, to:scope.file});
              } else {
                // TODO: Error that you can't drop folder into themselves
              }
              $rootScope.dragData = null;
            }
          });
        }
      }
    };
  }

  return {
    name: "drop",
    options: ['$timeout', drop]
  };
});
