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

define(
    [],
    function () {
      "use strict";
      return {
        isUnix: isUnix,
        isWindows: isWindows,
        convertWindowsPath: convertWindowsPath
      };

      function isUnix(path) {
        return path && path.indexOf("/") === 0;
      }

      function isWindows(path) {
        return path && (path.match(/[A-Za-z]:\\/) || path.match(/^\/[A-Za-z]:/));
      }

      function convertWindowsPath(path) {
        var parts = path.match(/^\/[A-Za-z]:/);
        if (parts) {
          var prefix = parts[0];
          return path.replace(prefix, prefix + "\\");
        }
        var index = path.indexOf("\\");
        var drive = path.substr(0, index + 1);
        var path = path.substr(index, path.length).replace(/\\/g, '/');
        if (path === '/') {
          path = "";
        }
        return "/" + drive + path;
      }
    });
