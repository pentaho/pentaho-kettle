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
  "../../../components/utils",
  "angular"
], function(utils, angular) {
  /**
   * @param {Service} $window - A reference to the browser's window object
   * @return {{restrict: string, link: link}} - resizeBreadcrumb directive
   */
  function resize($window, $state) {
    return {
      restrict: "AE",
      link: function(scope, element, attrs) {
        var w = angular.element($window);

        scope.$watch(attrs.folderParts, function(newValue, oldValue) {
          if (newValue !== oldValue) {
            _updateMaxWidths(_getInnerWidthCalc());
          }
        });

        w.on("resize", function() {
          _updateMaxWidths(_getInnerWidthCalc());
          scope.$apply();
        });

        /**
         * Gets the window inner width and compares it to the maximum allowable inner width
         * @return {number} - 440 if window inner width is less than the maximum allowed,
         * otherwise 440 minus the difference of max allowed and actual inner width
         * @private
         */
        function _getInnerWidthCalc() {
          var innerWidth = w[0].innerWidth;
          var maxInnerWidth = ($state.is("save") || $state.is("saveTo") || $state.is("saveToFileFolder")) ? 568 : 503;
          if (innerWidth < maxInnerWidth) {
            return (440 - (maxInnerWidth - innerWidth));
          }
          return 440;
        }

        /**
         * Calculates and sets the max width for 1, 2, or all 3 breadcrumb items.
         * Also, sets the tooltips if a breadcrumb is truncated.
         * @param {Number} maxWidth - Maximum width of entire breadcrumb area.
         * @private
         */
        function _updateMaxWidths(maxWidth) {
          var f1Width = 0;
          var f2Width = 0;
          var f3Width = 0;
          var mw0 = scope.vm.extras.length > 0 ? (maxWidth - 44) : maxWidth;
          var mw1 = 0;
          var mw2 = 0;
          var mw3 = 0;
          var title1 = "";
          var title2 = "";
          var title3 = "";
          switch (scope.vm.parts.length) {
            case 1:
              mw1 = mw0;
              title1 = utils.getTextWidth(scope.vm.parts[0].part) > mw1 ? scope.vm.parts[0].part : "";
              break;
            case 2:
              mw0 = Math.floor(mw0 / 2) - 14;
              mw1 = mw0;
              mw2 = mw0;
              f1Width = utils.getTextWidth(scope.vm.parts[0].part);
              f2Width = utils.getTextWidth(scope.vm.parts[1].part);
              if (f1Width < mw0) {
                mw1 = f1Width;
                mw2 = mw0 + mw0 - mw1;
                title1 = false;
              }
              title1 = f1Width > mw1 ? scope.vm.parts[0].part : "";
              title2 = f2Width > mw2 ? scope.vm.parts[1].part : "";
              break;
            case 3:
              mw0 = Math.floor(mw0 / 3) - 18;
              mw1 = mw0;
              mw2 = mw0;
              mw3 = mw0;
              f1Width = utils.getTextWidth(scope.vm.parts[0].part);
              f2Width = utils.getTextWidth(scope.vm.parts[1].part);
              f3Width = utils.getTextWidth(scope.vm.parts[2].part);
              if (f1Width < mw0) {
                mw1 = f1Width;
                mw2 = mw0 + (mw0 - mw1) / 2;
                mw3 = mw2;
              }
              if (f2Width < mw2) {
                mw2 = f2Width;
                mw3 += (mw3 - mw2);
              }
              title1 = f1Width > mw1 ? scope.vm.parts[0].part : "";
              title2 = f2Width > mw2 ? scope.vm.parts[1].part : "";
              title3 = f3Width > mw3 ? scope.vm.parts[2].part : "";
              break;
            default:
              break;
          }
          scope.vm.maxWidths = [mw1, mw2, mw3];
          scope.vm.titles = [title1, title2, title3];
        }
      }
    };
  }

  return {
    name: "resizeBreadcrumb",
    options: ["$window", "$state", resize]
  };
});
