/*!
 * Copyright 2017 Hitachi Vantara. All rights reserved.
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
    function() {
      "use strict";
      var _font = "14px OpenSansRegular";
      return {
        naturalCompare: naturalCompare,
        getTextWidth: getTextWidth,
        truncateString: truncateString
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
    });
