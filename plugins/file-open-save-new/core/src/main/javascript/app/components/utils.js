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
      var _font = "14px OpenSansRegular";
      return {
        naturalCompare: naturalCompare,
        getTextWidth: getTextWidth,
        truncateString: truncateString,
        buildParameters: buildParameters,
        concatParameters: concatParameters,
        getPlaceholder: getPlaceholder
      };

      /**
       * String comparison with arithmetic comparison of numbers
       * @param {String} first - The first string to compare
       * @param {String} second - The second string to compare
       * @return {number} - Returns -1, 1, or 0 according to the natural comparison of first and second
       **/
      function naturalCompare(first, second) {
        // any number ignoring preceding spaces
        var recognizeNbr = /[\\s]*[-+]?(?:(?:\d[\d,]*)(?:[.][\d]+)?|([.][\d]+))/;
        var idx1 = 0;
        var idx2 = 0;
        var sub1 = first;
        var sub2 = second;
        var match1;
        var match2;
        while (idx1 < sub1.length || idx2 < sub2.length) {
          sub1 = sub1.substring(idx1);
          sub2 = sub2.substring(idx2);
          // any numbers?
          match1 = sub1.match(recognizeNbr);
          match2 = sub2.match(recognizeNbr);
          if (match1 === null || match2 === null) {
            // treat as plain strings
            return _strComp(sub1, sub2);
          }
          // compare before match as string
          var pre1 = sub1.substring(0, match1.index);
          var pre2 = sub2.substring(0, match2.index);
          var comp = _strComp(pre1, pre2);
          if (comp !== 0) {
            return comp;
          }
          // compare numbers
          var num1 = new Number(match1[0]);
          var num2 = new Number(match2[0]);
          comp = (num1 < num2) ? -1 : (num1 > num2) ? 1 : 0;
          if (comp !== 0) {
            return comp;
          }
          // check after match
          idx1 = match1.index + match1[0].length;
          idx2 = match2.index + match2[0].length;
        }
        return 0;
      }

      /**
       * Performs localeCompare on str1 and str2
       * @param {String} str1 - first string
       * @param {String} str2 - second string
       * @return {number} - result of localeCompare
       * @private
       */
      function _strComp(str1, str2) {
        return str1.localeCompare(str2);
      }

      /**
       * Calculates the display width of the text parameter
       * @param {Object} text - String object to measure
       * @return {number} - width in pixels of text using font font
       */
      function getTextWidth(text) {
        var canvas = document.createElement("canvas");
        var context = canvas.getContext("2d");
        context.font = _font;
        var metrics = context.measureText(text);
        return (Math.ceil(metrics.width) + 2);
      }

      /**
       * Truncates a string to be less than length len (in terms of display length, not character length).
       * @param {String} str - string to be truncated
       * @param {Number} len - Maximum display length of str.
       * @return {String} - the truncated string.
       */
      function truncateString(str, len) {
        var res = str;
        if (str && str.length > 0) {
          var strLen = str.length;
          while (getTextWidth(res) > len) {
            res = str.slice(0, --strLen);
          }
        }
        return res;
      }

      function buildParameters(parameters) {
        var start = "?";
        for (var key in parameters) {
          var value = parameters[key];
          if (value !== undefined) {
            if (start.length > 1) {
              start += "&";
            }
            start += key + "=" + value;
          }
        }
        return start.length > 1 ? start : "";
      }

      function concatParameters(first, second) {
        for (var key in first) {
          second[key] = first[key];
        }
        return second;
      }

      /**
       * Determines if the browser is Internet Explorer.
       * If it is, it truncates the placeholder for the search box if it's width is greater than the
       * search box. It then adds ellipsis to the end of that string and returns that value.
       * If it is not Internet Explorer, it just returns the search box placeholder and any
       * truncation/ellipsis is handled using CSS. NOTE: this is a workaround for an IE bug
       * that doesn't allow placeholders to be ellipsis unless the input is readonly.
       * @return {string} - the Placeholder for the search box
       */
      function getPlaceholder(placeholder, folder, currentRepo) {
        var isIE = navigator.userAgent.indexOf("Trident") !== -1 && Boolean(document.documentMode);
        var retVal = placeholder;
        if (folder.path !== "Recents") {
          if (folder.name) {
            retVal += " " + folder.name;
          } else {
            retVal += " " + folder.path.substr(folder.path.lastIndexOf("/") + 1, folder.path.length);
          }
        } else {
          retVal += " " + currentRepo;
        }
        if (isIE && getTextWidth(retVal) > 210) {
          var tmp = "";
          for (var i = 0; i < retVal.length; i++) {
            tmp = retVal.slice(0, i);
            if (getTextWidth(tmp) > 196) {
              break;
            }
          }
          retVal = tmp + "...";
        }
        return retVal;
      }
    });
