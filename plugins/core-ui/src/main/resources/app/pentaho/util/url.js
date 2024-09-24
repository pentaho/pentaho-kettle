/*!
 * Copyright 2017-2018 Hitachi Vantara. All rights reserved.
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
  "./has"
], function(has) {
  "use strict";

  /* eslint new-cap: 0 */

  /**
   * The `url` namespace contains functions for handling [URLs]{@link URL}.
   *
   * @name url
   * @namespace
   * @memberOf pentaho.util
   * @amd pentaho/util/url
   * @private
   */
  return /** @lends pentaho.util.url */{
    /**
     * Creates an {@link URL} object.
     *
     * If the specified {@code url} is relative, it will be resolved
     * with the current location.
     *
     * As a fallback, in case the [URL constructor]{@link URL} is not
     * available, {@code url} is going to be parsed and a URL mock
     * will be returned instead.
     *
     * @param {?string} url - A resource location.
     *
     * @return {?URL} the new URL object.
     */
    create: createUrl,

    /**
     * Parses a {@code url} string.
     *
     * Returns an array with the following {@link URL} parameters:
     *  1. protocol (required)
     *  2. authority (userName:password) (optional)
     *  3. host (optional)
     *  4. port (optional)
     *  5. pathname (optional)
     *
     * @param {string} url - A resource location.
     *
     * @return {Array.<string>} the parsed url.
     */
    parse: parseUrl
  };

  function createUrl(url) {
    if(!url) {
      return null;
    }

    if(has("URL")) {
      return new URL(url, document.location);
    }

    // Return a MOCK URL
    var parsedUrl = parseUrl(url) ||

      // tests can reach here, as URL is fed from CONTEXT_PATH, which is usually not absolute
      parseUrl((url = __makeAbsoluteUrl(url))) ||

      // TODO: CGG/rhino can reach here, as its createElement is mocked. Remove when the latter dies.
      // Assume the whole url is the pathname.
      [url, "", null, "", null, url];

    var protocol = parsedUrl[1] || "";
    var auth = !!parsedUrl[2] ? parsedUrl[2].slice(0, -1).split(":") : "";
    var host = parsedUrl[3] || "";
    var port = !!parsedUrl[4] ? parsedUrl[4].substring(1) : "";
    var pathname = parsedUrl[5] || "";

    return {
      href:     url,
      protocol: protocol,
      username: auth.length > 0 ? auth[0] : "",
      password: auth.length > 1 ? auth[1] : "",
      hostname: host,
      host: host + ":" + port,
      port: port,
      origin: protocol + "//" + host + ":" + port,
      pathname: pathname,

      toString: function() {
        return url;
      }
    };

  }

  function parseUrl(url) {
    return /^\s*([^:\/?#]+:)\/\/([^@]*@)?([^:\/?#]*)(:\d*)?(\/[^?#]*)+/.exec(url);
  }

  function __makeAbsoluteUrl(url) {
    var aElem = document.createElement("a");
    aElem.href = url;
    return aElem.href;
  }

});
