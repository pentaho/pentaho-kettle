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
   * @param {Service} $window - A reference to the browser's window object
   * @param {Function} $timeout - Angular wrapper for window.setTimeout.
   * @param {Object} $state - The application state object
   * @return {{restrict: string, link: link}} - resizeFiles directive
   */
  function resize($window, $timeout, $state) {
    return {
      restrict: "A",
      link: function(scope, element, attrs) {
        var scrollClass = "";
        $timeout(function() {
          scrollClass = ($state.is("save") || $state.is("saveTo") || $state.is("saveToFileFolder")) ? "scrollTableSave" : "scrollTableOpen";
        });

        var table = angular.element(element[0].querySelector("#filesTableBody"));
        var bodyWrapper = angular.element(element[0].querySelector("#bodyWrapper"));
        var headerWrapper = angular.element(document.querySelector("#headerWrapper"));
        var w = angular.element($window);

        scope.$watch(attrs.resizeFiles, function(newValue, oldValue) {
          if (newValue !== oldValue) {
            $timeout(function() {
              setScrollTableClass();
              setWidths();
            }, 1);
          }
        });

        scope.$watch(attrs.searchValue, function(newValue) {
          $timeout(function() {
            setScrollTableClass();
            setWidths();
          });
        });

        scope.$watch(attrs.selectedFile, function(newValue) {
          if (newValue === null) {
            $timeout(function() {
              setScrollTableClass();
              setWidths();
            });
          }
        });

        /**
         * Sets the class of the element if scrolling is needed. Also, sets css for the elements within file area.
         */
        function setScrollTableClass() {
          if (!scope.vm.folder) {
            return;
          }
          bodyWrapper.css("height", "calc(100% - 31px)");
          table.css("margin-bottom", "0");
          var hasVertScroll = bodyWrapper[0].scrollHeight > bodyWrapper[0].clientHeight;
          if (hasVertScroll) {
            table.css("min-width", "612px");
            bodyWrapper.css("height", "calc(100% - 31px)");
            table.css("margin-bottom", "0");
            element.addClass(scrollClass);
          } else {
            table.css("min-width", "629px");
            bodyWrapper.css("height", "auto");
            var spacing = bodyWrapper[0].scrollWidth > bodyWrapper[0].clientWidth ? 17 : 0;
            table.css("margin-bottom", (element[0].clientHeight - table[0].clientHeight - 31 - spacing) + "px");
            element.removeClass(scrollClass);
          }
        }

        /**
         * Sets the width of this element and the header/body wrappers in the file area.
         * The width is the remaining space from the window width - the directory area.
         */
        function setWidths() {
          var innerWidth = w[0].innerWidth;
          var dirTreeAreaWidth = angular.element(document.querySelector("#directoryTreeArea"))[0].offsetWidth - 1;
          var widthToSet = (innerWidth - dirTreeAreaWidth - 1) + "px";
          element.css("width", widthToSet);
          headerWrapper.css("width", widthToSet);
          bodyWrapper.css("width", widthToSet);
          setHeaderWidthAndPos();
        }

        /**
         * Since the header needs to scroll with the body, we need to handle the width
         */
        function setHeaderWidthAndPos() {
          var left = -1 * bodyWrapper[0].scrollLeft;
          var width = bodyWrapper[0].clientWidth + (-1 * left);
          var buffer = bodyWrapper[0].scrollHeight > bodyWrapper[0].clientHeight ? 17 : 0;
          headerWrapper.css("left", left + "px");
          headerWrapper.css("width", (width + buffer) + "px");
        }

        w.on("resize", function() {
          setScrollTableClass();
          setWidths();
          scope.$apply();
        });

        bodyWrapper.on("scroll", setHeaderWidthAndPos);
      }
    };
  }

  return {
    name: "resizeFiles",
    options: ["$window", "$timeout", "$state", resize]
  };
});
