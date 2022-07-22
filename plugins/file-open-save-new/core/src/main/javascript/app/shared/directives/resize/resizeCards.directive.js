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
   * @return {{restrict: string, link: link}} - resizeCards directive
   */
  function resize($window, $timeout) {
    return {
      restrict: "A",
      link: function(scope, element, attrs) {
        var w = angular.element($window);
        var recentsView = element[0].parentElement;

        scope.$watch(attrs.resizeCards, function(newValue) {
          if (newValue) {
            $timeout(function() {
              resize();
            }, 10);
          }
        });

        w.on("resize", function() {
          resize();
          scope.$apply();
        });

        /**
         * Handles resizing the Recents view.
         */
        function resize() {
          var clientWidth = recentsView.clientWidth;
          var hasScrolling = recentsView.scrollHeight > recentsView.clientHeight;
          var cards = angular.element(element.children());
          var cardTitles = angular.element(document.getElementsByClassName("cardTitle"));
          if (clientWidth < 321 && hasScrolling && cards) {
            cards.css("width", (clientWidth - 37) + "px");
            cardTitles.css("max-width", (clientWidth - 37 - 92) + "px");
          } else if (clientWidth < 324 && !hasScrolling) {
            cards.css("width", (clientWidth - 40) + "px");
            cardTitles.css("max-width", (clientWidth - 40 - 92) + "px");
          } else {
            cards.css("width", "284px");
            cardTitles.css("max-width", "");
          }
        }
      }
    };
  }

  return {
    name: "resizeCards",
    options: ["$window", "$timeout", resize]
  };
});
