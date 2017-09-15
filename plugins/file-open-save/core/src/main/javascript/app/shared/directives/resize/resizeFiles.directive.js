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
   * @return {{restrict: string, link: link}} - resizeFiles directive
   */
  function resize($window) {
    return {
      restrict: "A",
      link: function(scope, element, attrs) {
        var _filesHeaderHeight = 31;
        var _openSaveDiff = 73;
        var _openOtherHeight = 220;
        var _saveOtherHeight = _openOtherHeight + _openSaveDiff;
        var _openOtherMaxHeight = 251;
        var _saveOtherMaxHeight = _openOtherMaxHeight + _openSaveDiff;
        var _buffer = 17;
        var openOrSave = scope.vm.wrapperClass;
        var w = angular.element($window);

        scope.$watch(attrs.resizeFiles, function(newValue, oldValue) {
          if (newValue !== oldValue) {
            setScrollTableClass();
          }
        });

        /**
         * Sets the class of the element if scrolling is needed
         */
        function setScrollTableClass() {
          var folder = scope.vm.folder;
          if (folder.name === "Recents" && folder.path === "Recents") {
            return;
          }
          var filesLength = folder.children.length;
          var isSearch = scope.vm.search;
          var numResults = scope.vm.numResults;
          var totalFileHeight = 0;
          var bodyWrapperHeight = 0;
          var scrollClass = "";
          if (isSearch) {// searching
            totalFileHeight = numResults * 60 + _buffer;
          } else {// not searching
            totalFileHeight = filesLength * 30 + _buffer;
          }
          if (openOrSave === "open") {
            bodyWrapperHeight = w[0].innerHeight - _openOtherHeight - _filesHeaderHeight;
            scrollClass = "scrollTableOpen";
          } else if (openOrSave === "save") {
            bodyWrapperHeight = w[0].innerHeight - _saveOtherHeight - _filesHeaderHeight;
            scrollClass = "scrollTableSave";
          }
          if (bodyWrapperHeight < totalFileHeight) {
            element.addClass(scrollClass);
          } else {
            element.removeClass(scrollClass);
          }
        }

        w.on("resize", function() {
          var innerHeight = w[0].innerHeight;
          var _tmpBuffer = w[0].innerWidth < 930 ? _buffer : 0;
          var elem = angular.element(element[0].querySelector("#bodyWrapper"));
          setScrollTableClass();
          if (openOrSave === "open") {
            elem.css("max-height", (innerHeight - _openOtherMaxHeight - _tmpBuffer) + "px");
          } else if (openOrSave === "save") {
            elem.css("max-height", (innerHeight - _saveOtherMaxHeight - _tmpBuffer) + "px");
          }
          scope.$apply();
        });
      }
    };
  }

  return {
    name: "resizeFiles",
    options: ["$window", resize]
  };
});
