/*!
 * Copyright 2018-2020 Hitachi Vantara. All rights reserved.
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

/**
 * The animation provider for the repository manager
 *
 */
define(
    [],
    function () {
      'use strict';

      var factoryArray = ["$rootScope", "$state", "$transitions", factory];
      var module = {
        class: ".transition",
        factory: factoryArray
      };

      return module;

      /**
       * The appAnimation factory
       *
       * @param {Object} $rootScope
       * @returns {Object} The callbacks for animation
       */
      function factory($rootScope, $state, $transitions) {
        var transition = $state.current.name === "connecting" ? "fade" : "slideLeft";
        var transitions = {
          "add->other": "fade",
          "other->add": "fade",
          "pentaho.details->add": "slideRight",
          "pentaho.details->manager": "slideRight",
          "pentaho.failure->pentaho.details": "slideRight",
          "pentaho.failure->manager": "slideRight",
          "pentaho.success->manager": "slideRight",
          "database.details->other": "slideRight",
          "database.details->manager": "slideRight",
          "database.select->database.details": "slideRight",
          "database.failure->database.details": "slideRight",
          "database.failure->manager": "slideRight",
          "database.success->manager": "slideRight",
          "file.details->other": "slideRight",
          "file.details->manager": "slideRight",
          "file.failure->file.details": "slideRight",
          "file.failure->manager": "slideRight",
          "file.success->manager": "slideRight",
          "connect->connecting": "fade",
          "connecting->connect": "fade"
        };

        $transitions.onStart({ }, function(trans) {
          var next = transitions[trans.from().name + "->" + trans.to().name];
          if (next) {
            transition = next;
          } else {
            transition = "slideLeft";
          }
        });

        return {
          enter: enter,
          leave: leave
        };

        function enter(element, done) {
          switch (transition) {
            case "slideLeft":
              _slideLeftEnter(element, done);
              break;
            case "slideRight":
              _slideRightEnter(element, done);
              break;
            case "fade":
              _fadeEnter(element, done);
              break;
          }
        }

        function leave(element, done) {
          switch (transition) {
            case "slideLeft":
              _slideLeftLeave(element, done);
              break;
            case "slideRight":
              _slideRightLeave(element, done);
              break;
            case "fade":
              _fadeLeave(element, done);
              break;
          }
        }

        function _slideLeftEnter(element, done) {
          jQuery(element).css('left', '100%');
          jQuery(element).animate({
            left: 0
          }, function () {
            done();
          });
        }

        function _slideRightEnter(element, done) {
          jQuery(element).css('left', '-100%');
          jQuery(element).animate({
            left: 0
          }, function () {
            done();
          });
        }

        function _fadeEnter(element, done) {
          jQuery(element).css("opacity", 0);
          jQuery(element).animate({
            opacity: 1
          }, function() {
            done();
          });
        }

        function _slideLeftLeave(element, done) {
          jQuery(element).css('left', 0);
          jQuery(element).animate({
            left: "-100%"
          }, function() {
            done();
          });
        }

        function _slideRightLeave(element, done) {
          jQuery(element).css('left', 0);
          jQuery(element).animate({
            left: '100%'
          }, function () {
            done();
          });
        }

        function _fadeLeave(element, done) {
          jQuery(element).css("opacity", 1);
          jQuery(element).animate({
            opacity: 0
          }, function() {
            done();
          });
        }
      }
    });
