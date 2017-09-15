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
  "angular"
], function(angular) {
  resize.$inject = ["$window"];
  /**
   * @param {Service} $window - A reference to the browser's window object
   * @return {{restrict: string, link: link}} - resizeApp directive
   */
  function resize($window) {
    return {
      restrict: "A",
      link: function(scope, element, attrs) {
        var _openSaveDiff = 73;
        var _openOtherHeight = 220;
        var _saveOtherHeight = _openOtherHeight + _openSaveDiff;
        var openOrSave = attrs.resizeApp;
        var w = angular.element($window);

        w.on("resize", function() {
          var innerHeight = w[0].innerHeight;
          if (openOrSave === "open") {
            element.css("height", (innerHeight - _openOtherHeight) + "px");
          } else if (openOrSave === "save") {
            element.css("height", (innerHeight - _saveOtherHeight) + "px");
          }
          scope.$apply();
        });
      }
    };
  }

  return {
    name: "resizeApp",
    options: ["$window", resize]
  };
});
