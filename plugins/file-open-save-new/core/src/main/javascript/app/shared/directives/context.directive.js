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
], function (angular) {
  /**
   * @param {Function} $document - Angular wrapper for window.document.
   * @return {Object} - context directive
   */

  function context($document) {
    return {
      restrict: "A",
      link: function (scope, element, attrs) {
        var contextMenu = angular.element(document.querySelector('#' + attrs.context));
        element.bind('contextmenu', function (event) {
          scope.$apply(function () {
            event.stopPropagation();
            event.preventDefault();
            var contextHeight = contextMenu.outerHeight();
            var contextWidth = contextMenu.outerWidth();
            var documentHeight = $document.height();
            var documentWidth = $document.width();
            var contextY = event.clientY + contextHeight > documentHeight ? documentHeight - contextHeight : event.clientY;
            var contextX = event.clientX + contextWidth > documentWidth ? documentWidth - contextWidth : event.clientX;
            contextMenu.css({
              'display': 'block',
              'top': contextY + 'px',
              'left': contextX + 'px'
            })
          });
        });
        angular.element($document).bind('click', function (event) {
          contextMenu.css({
            'display': 'none'
          });
        });
        angular.element($document).bind('keydown', function (event) {
          if (event.keyCode === 27) {
            contextMenu.css({
              'display': 'none'
            });
          }
        });
      }
    };
  }

  return {
    name: "context",
    options: ["$document", context]
  };
});
