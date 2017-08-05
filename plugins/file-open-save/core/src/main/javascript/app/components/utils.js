/*!
 * Copyright 2017 Pentaho Corporation. All rights reserved.
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
      'use strict';
      return {
        naturalCompare: naturalCompare,
        getTextWidth: getTextWidth
      };

    /**
     * String comparison with arithmetic comparison of numbers
     **/
    function naturalCompare(first, second) {
      // any number ignoring preceding spaces
      var recognizeNbr = /[\\s]*[-+]?(?:(?:\d[\d,]*)(?:[.][\d]+)?|([.][\d]+))/;
      var idx1 = 0, idx2 = 0;
      var sub1 = first, sub2 = second;
      var match1, match2;
      while( idx1 < sub1.length || idx2 < sub2.length ) {
        sub1 = sub1.substring(idx1);
        sub2 = sub2.substring(idx2);
        // any numbers?
        match1 = sub1.match(recognizeNbr);
        match2 = sub2.match(recognizeNbr);
        if ( match1 == null || match2 == null ) {
          // treat as plain strings
          return strComp(sub1, sub2);
        }
        // compare before match as string
        var pre1 = sub1.substring(0, match1.index);
        var pre2 = sub2.substring(0, match2.index);
        var comp = strComp(pre1, pre2);
        if ( comp != 0 ) {
          return comp;
        }
        // compare numbers
        var num1 = new Number( match1[0] );
        var num2 = new Number( match2[0] );
        comp = (num1 < num2) ? -1 : (num1 > num2) ? 1 : 0;
        if(comp != 0) {
          return comp;
        }
        // check after match
        idx1 = match1.index + match1[0].length;
        idx2 = match2.index + match2[0].length;
      }
      return 0;
    }

    function strComp(str1, str2) {
      return str1.localeCompare(str2);
    }

      /**
       * Calculates the display width of the text parameter
       * @param {Object} text - String object to measure
       * @param {Object} font - String object for the font being used
       * @return {number} - width in pixels of text using font font
       */
      function getTextWidth(text, font) {
        var canvas = document.createElement("canvas");
        var context = canvas.getContext("2d");
        context.font = font;
        var metrics = context.measureText(text);
        return (Math.ceil(metrics.width) + 2);
      }
  });
