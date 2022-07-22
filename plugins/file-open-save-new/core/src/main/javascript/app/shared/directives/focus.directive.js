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
], function(angular) {
  /**
   * @param {Function} $timeout - Angular wrapper for window.setTimeout.
   * @return {{restrict: string, scope: {model: string}, link: link}} - Focus directive
   */
  function focus($timeout) {
    return {
      restrict: 'A',
      scope: {model: '<ngModel'},
      link: function(scope, element, attrs) {
        var watch = scope.$watch("model", function(value) {
          if (value === "") {
            return;
          }
          $timeout(function() {
            element[0].focus();
            element[0].select();
            if (value) {
              element[0].setSelectionRange(0, value.length);
            }
            watch();
          });
        });
      }
    };
  }

  return {
    name: "focus",
    options: ['$timeout', focus]
  };
});
