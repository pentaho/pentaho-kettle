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
   * @param {Object} $document - jQuery or jqLite wrapper for the browser's window.document object.
   * @return {{restrict: string, link: link}} - Key Directive
   */
  function key($document) {
    return {
      restrict: 'AE',
      link: function(scope, element) {
        var errorWithConfirm = [1, 5, 6];
        var confirmButton = angular.element(element[0].querySelector("#errorConfirmButton"));
        var cancelButton = angular.element(element[0].querySelector("#errorCancelButton"));

        var stopEvents = function(event) {
          event.stopPropagation();
          event.preventDefault();
          event.returnValue = false;
          event.cancelBubble = true;
          return false;
        };

        $document.on("keydown", function(event) {
          handleKeyDownAndKeyPress(event);
        });

        $document.on("keypress", function(event) {
          handleKeyDownAndKeyPress(event);
        });

        $document.on("keyup", function(event) {
          handleKeyUp(event);
        });

        /**
         * Function to handle keydown and keypress events
         * @param {Object} event - DOM event
         */
        function handleKeyDownAndKeyPress(event) {
          if (scope.vm.errorType === 0) {
            scope.vm.onKeyDown(event);
            scope.$apply();
          } else if (scope.vm.errorType !== 0) {
            stopEvents(event);
          }
        }

        /**
         * Function to handle key events
         * @param {Object} event - DOM event
         */
        function handleKeyUp(event) {
          if (scope.vm.errorType === 0) {
            scope.vm.onKeyUp(event);
            scope.$apply();
          } else if (event.keyCode === 13) {
            handleEnter(scope.vm.errorType);
          }
        }

        /**
         * Function to handle key events
         * @param {Number} errorType - Number to designate the type of error
         */
        function handleEnter(errorType) {
          if (errorWithConfirm.indexOf(errorType) === -1) {
            cancelButton[0].click();
          } else {
            confirmButton[0].click();
          }
        }
      }
    };
  }

  return {
    name: "key",
    options: ["$document", key]
  };
});
