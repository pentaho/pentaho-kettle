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
], function() {
  key.$inject = ["$document"];

  /**
   * @param {Object} $document - jQuery or jqLite wrapper for the browser's window.document object.
   * @return {{restrict: string, scope: {onKeyUp: string}, link: link}} - Key Directive
   */
  function key($document) {
    return {
      restrict: "AE",
      scope: {
        onKeyUp: "&"
      },
      link: function(scope, element, attr) {
        $document.on("keyup", function(event) {
          scope.onKeyUp({event: event});
        });
      }
    };
  }

  return {
    name: "key",
    options: ["$document", key]
  };
});
