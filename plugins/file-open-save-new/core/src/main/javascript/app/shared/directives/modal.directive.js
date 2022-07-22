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
  "angular",
    "../../services/modal.service"
], function(angular, modalService) {
  /**
   * @return {Object} - modal directive
   */

  function modal($parse, $q, modalService) {
    return {
      restrict: "E",
      link: function(scope, element, attrs) {
        var backdrop = angular.element("<div class='backdrop'></div>");
        angular.element(document.body).append(backdrop);

        function openModal(title, body) {
          scope.title = title;
          scope.body = body;
          backdrop.show();
          element.show();
          return $q(function(resolve, reject) {
            var buttons = element[0].getElementsByTagName('button');
            var inputs = element[0].getElementsByTagName('input');
            var inputValues = [];
            for (var i = 0; i < inputs.length; i++) {
              var input = angular.element(inputs[i]);
              input[0].checked = false;
              input.data("index", i);
              input.bind("change", function() {
                inputValues[this.getAttribute('id')] = this.checked;
              });
            }
            for (var i = 0; i < buttons.length; i++) {
              var button = angular.element(buttons[i]);
              button.data("index", i);
              button.bind("click", function() {
                var value = this.getAttribute("value");
                closeModal();
                resolve({
                  values: inputValues,
                  button: value
                });
              });
            }
          });
        }

        function setBody(body) {
          scope.body = body;
        }

        function closeModal() {
          backdrop.hide();
          element.hide();
        }

        modalService.add({id: attrs.id, open: openModal, close: closeModal, setBody: setBody});
      }
    };
  }

  return {
    name: "modal",
    options: ["$parse", "$q", modalService.name, modal]
  };
});
