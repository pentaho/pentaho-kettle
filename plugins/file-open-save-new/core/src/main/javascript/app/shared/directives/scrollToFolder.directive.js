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
   * @return {{restrict: string, scope: {model: string}, link: link}} - scrollToFolder directive
   */
  function scrollToFolder($timeout) {
    return {
      restrict: "A",
      scope: {model: "<ngModel", delete: "=didDelete"},
      link: function(scope, element, attrs) {
        scope.$watch("model", function(folder) {
          if (!folder || folder.path === "" || folder.path === "Recents") {
            return;
          }
          $timeout(function() {
            scrollToSelectedFolder();
          }, 1);
        });

        function scrollToSelectedFolder() {
          var selectedFolders = element[0].getElementsByClassName("selected");
          if (selectedFolders.length === 0 || selectedFolders[0].offsetParent === null) {
            return;
          }
          var selected = angular.element(selectedFolders[0]);
          var selectedTop = selected[0].offsetTop;
          var selectedHeight = selected[0].offsetHeight;

          var containerHeight = element[0].offsetHeight;
          var scrollTop = element[0].scrollTop;

          var actualTop = selectedTop - scrollTop;
          var actualBottom = selectedTop - scrollTop + selectedHeight;
          if ( actualBottom > containerHeight || actualTop < 0 ) {
            element[0].scrollTop = selectedTop - containerHeight / 2 + selectedHeight / 2;
          }
        }
      }
    };
  }

  return {
    name: "scrollToFolder",
    options: ["$timeout", scrollToFolder]
  };
});
