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
  'angular'
], function (angular) {

  function showpassword() {
    return {
      restrict: 'AE',
      scope: {},
      link: function (scope, element, attr) {
        element.css('background-image', 'url("img/show-inactive.svg")');
        element.css('background-repeat', 'no-repeat');
        element.css('background-size', '16px 16px');
        element.css('padding-right', '30px');
        element.css('background-position', (element.outerWidth() - 25) + 'px center');
        element.bind('click', function (e) {
          if (e.offsetX > element.outerWidth() - 30) {
            var type = element.prop('type');
            element.prop({type: type === "text" ? "password" : "text"});
            element.css('background-image', type === "text" ? 'url("img/show-inactive.svg")' : 'url("img/hide-inactive.svg")');
          }
        });
        element.bind('mousemove', function (e) {
          if (e.offsetX > element.outerWidth() - 30) {
            element.css('cursor', 'pointer');
          } else {
            element.css('cursor', 'auto');
          }
        });
      }
    }
  }

  return {
    name: "showpassword",
    options: [showpassword]
  }
});
