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
  'text!./message.html',
  'pentaho/i18n-osgi!repositories-plugin.messages',
  'css!./message.css'
], function (template, i18n, $) {

  'use strict';

  var options = {
    bindings: {
      message: "<"
    },
    controllerAs: "vm",
    template: template,
    controller: messageController
  };

  messageController.$inject = ["$timeout"];

  function messageController($timeout) {
    var vm = this;
    vm.$onChanges = onChanges;
    vm.reset = reset;
    vm.showMessage = false;

    var timeout;
    var messageElement = angular.element("#message");

    function onChanges(changes) {
      if (changes.message && changes.message.currentValue == null) {
        if (vm.showMessage) {
          reset();
        }
      } else if (changes.message && changes.message.currentValue) {
        vm.text = changes.message.currentValue.text;
        vm.type = changes.message.currentValue.type;
        if (vm.showMessage) {
          reset(show);
        } else {
          show();
        }
      }
    }

    function show() {
      if (timeout) {
        $timeout.cancel(timeout);
      }
      vm.showMessage = true;
      messageElement.animate({
        opacity: 1,
        top: 20
      }, 600, "linear", function () {
        timeout = $timeout(function () {
          if (vm.showMessage) {
            reset();
          }
        }, 5000);
      });
    }

    function reset(callback) {
      if (timeout) {
        $timeout.cancel(timeout);
      }
      vm.showMessage = false;
      messageElement.animate({
        opacity: 0
      }, 600, function () {
        if (callback) {
          callback();
        } else {
          messageElement.css("top", "-63px");
        }
      });
    }
  }

  return {
    name: "message",
    options: options
  };

});
