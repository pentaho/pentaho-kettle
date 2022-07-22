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

/**
 * The Services Service
 *
 * The Services Service, a collection of endpoints used by the application
 *
 * @module services/services.service
 * @property {String} name The name of the module.
 */
define(
    [],
    function () {
      "use strict";

      var factoryArray = [factory];
      var module = {
        name: "modalService",
        factory: factoryArray
      };

      return module;

      /**
       * The fileService factory
       *
       * @return {Object} The fileService api
       */
      function factory() {

        var modals = [];

        return {
          add: add,
          open: open,
          setBody: setBody,
          close: close
        };

        /**
         *
         * @param modal
         */
        function add(modal) {
          modals.push(modal);
        }

        /**
         *
         * @param id
         * @returns {*}
         * @private
         */
        function _getModalById(id) {
          for (var i = 0; i < modals.length; i++) {
            if (modals[i].id === id) {
              return modals[i]
            }
          }
        }

        /**
         *
         * @param id
         * @param title
         * @param body
         * @returns {*}
         */
        function open(id, title, body) {
          return _getModalById(id).open(title, body);
        }

        /**
         *
         * @param id
         */
        function close(id) {
          _getModalById(id).close();
        }

        /**
         *
         * @param id
         * @param body
         */
        function setBody(id, body) {
          _getModalById(id).setBody(body);
        }
      }
    });
