/*!
 * Copyright 2019-2021 Hitachi Vantara. All rights reserved.
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
 * A helper Service
 *
 * A helper Service, a collection of http helper services
 *
 * @module services/helper.service
 * @property {String} name The name of the module.
 */
define(
    [],
    function() {
      "use strict";

      var factoryArray = ["$http", factory];
      var module = {
        name: "helperService",
        factory: factoryArray
      };

      return module;

      /**
       * The dataService factory
       *
       * @param {Object} $http - The $http angular helper service
       *
       * @return {Object} The dataService api
       */
      function factory($http) {
        return {
          httpGet: httpGet,
          httpPost: httpPost,
          httpPut: httpPut,
          httpDelete: httpDelete
        };

        /**
         * Wraps the http angular service to provide sensible defaults
         *
         * @param {String} url - the url
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @private
         */
        function httpGet(url) {
          return _wrapHttp("GET", url);
        }

        /**
         * Wraps the http angular service to provide sensible defaults
         *
         * @param {String} url - the url
         * @param {String} data - the post data
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @private
         */
        function httpPost(url, data) {
          return _wrapHttp("POST", url, data);
        }

        /**
         * Wraps the http angular service to provide sensible defaults
         *
         * @param {String} url - the url
         * @param {String} data - the put data
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @private
         */
        function httpPut(url, data) {
          return _wrapHttp("PUT", url, data);
        }

        /**
         * Wraps the http angular service to provide sensible defaults
         *
         * @param {String} url - the url
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @param {String} data - the data to send to the server
         * @private
         */
        function httpDelete(url, data) {
          return _wrapHttp("DELETE", url, data);
        }

        /**
         * Wraps the http angular service to provide sensible defaults
         *
         * @param {String} method - the http method to use
         * @param {String} url - the url
         * @param {String} data - the data to send to the server
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @private
         */
        function _wrapHttp(method, url, data) {
          var options = {
            method: method,
            url: ( ( IS_RUNNING_ON_WEBSPOON_MODE ) ? CONTEXT_PATH : "" ) + _cacheBust(url),
            headers: {
              Accept: "application/json"
            }
          };
          if (data !== null) {
            options.data = data;
          }
          return $http(options);
        }

        /**
         * Eliminates cache issues
         * @param {String} url - url string
         * @return {*} - url
         * @private
         */
        function _cacheBust(url) {
          var value = Math.round(new Date().getTime() / 1000) + Math.random();
          if (url.indexOf("?") !== -1) {
            url += "&v=" + value;
          } else {
            url += "?v=" + value;
          }
          if ( IS_RUNNING_ON_WEBSPOON_MODE ) {
            var cid = getConnectionId();
            url += "&cid=" + cid;
          }
          return url;
        }
      }
    });
