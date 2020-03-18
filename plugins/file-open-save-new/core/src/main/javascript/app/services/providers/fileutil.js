/*!
 * Copyright 2020 Hitachi Vantara. All rights reserved.
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
        getPathSep: getPathSep,
        getFilename: getFilename,
        getParentPath: getParentPath,
        cleanPath: cleanPath,
        concatPath: concatPath,
        replaceFilename: replaceFilename,
        convertWindowsPath: convertWindowsPath
      };

      function isUnix(path) {
        return path && path.indexOf("/") === 0;
      }

      function isWindows(path) {
        return path && (path.match(/[A-Za-z]:\\/) || path.match(/^\/[A-Za-z]:/));
      }

      /**
       * Returns the path separator base on inspecting the path
       * @param path
       * @returns {string}
       */
      function getPathSep(path) {
        return isWindows(path) ? "\\" : "/";
      }

      /**
       * Returns the filename of a given path.
       * @param path
       * @returns {string}
       */
      function getFilename(path) {
        return path.substr(path.lastIndexOf(getPathSep(path)) + 1, path.length);
      }

      /**
       * Returns the parent path directory of a given path.
       * @param path
       * @returns {string}
       */
      function getParentPath(path) {
        return path.substr(0, path.lastIndexOf(getPathSep(path)));
      }

      /**
       * Removes any trailing path separator for a given path.
       * @param path
       * @returns {any}
       */
      function cleanPath(path) {
        return path.lastIndexOf(getPathSep(path)) === path.length - 1 ? path.substr(0, path.length - 1) : path;
      }


      /**
       * Adds a filename using the matching path separator
       * @param path
       * @param filename
       * @returns {string}
       */
      function concatPath(path, filename) {
        return path + getPathSep(path) + filename;
      }

      /**
       * Returns a filepath with a filename replaced
       * @param path - path to be updated
       * @param filename - filename to replace with
       * @returns {string}
       */
      function replaceFilename(path, filename) {
        var pathsep = getPathSep(path);
        return path.substr( 0, path.lastIndexOf(pathsep)) + pathsep + filename;
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
