define(['angular',
      'angular-translate'
    ],

    function(angular) {
      'use strict';

      angular.module('pascalprecht.translate')
      /**
       * @ngdoc object
       * @name pascalprecht.translate.$translateStaticFilesLoader
       * @requires $q
       * @requires $http
       *
       * @description
       * Creates a loading function for a typical static file url pattern:
       * "lang-en_US.json", "lang-de_DE.json", etc. Using this builder,
       * the response of these urls must be an object of key-value pairs.
       *
       * @param {object} options Options object, which gets prefix, suffix and key.
       */
          .factory('$translateStaticFilesLoader', ['$http', function($http) {

            var rWhitespace = /\s/;

            function Iterator(text) {
              var pos = 0, length = text.length;

              this.peek = function(num) {
                num = num || 0;
                if (pos + num >= length) {
                  return null;
                }

                return text.charAt(pos + num);
              };
              this.next = function(inc) {
                inc = inc || 1;

                if (pos >= length) {
                  return null;
                }

                return text.charAt((pos += inc) - inc);
              };
              this.pos = function() {
                return pos;
              };
            }

            function isWhitespace(chr) {
              return rWhitespace.test(chr);
            }

            function consumeWhiteSpace(iter) {
              var start = iter.pos();

              while (isWhitespace(iter.peek())) {
                iter.next();
              }

              return {type: "whitespace", start: start, end: iter.pos()};
            }

            function startsComment(chr) {
              return chr === "!" || chr === "#";
            }

            function isEOL(chr) {
              return chr == null || chr === "\n" || chr === "\r";
            }

            function consumeComment(iter) {
              var start = iter.pos();

              while (!isEOL(iter.peek())) {
                iter.next();
              }

              return {type: "comment", start: start, end: iter.pos()};
            }

            function startsKeyVal(chr) {
              return !isWhitespace(chr) && !startsComment(chr);
            }

            function startsSeparator(chr) {
              return chr === "=" || chr === ":" || isWhitespace(chr);
            }

            function startsEscapedVal(chr) {
              return chr === "\\";
            }

            function consumeEscapedVal(iter) {
              var start = iter.pos();

              iter.next(); // move past "\"
              var curChar = iter.next();
              if (curChar === "u") { // encoded unicode char
                iter.next(4); // Read in the 4 hex values
              }

              return {type: "escaped-value", start: start, end: iter.pos()};
            }

            function consumeKey(iter) {
              var start = iter.pos(), children = [];

              var curChar;
              while ((curChar = iter.peek()) !== null) {
                if (startsSeparator(curChar)) {
                  break;
                }
                if (startsEscapedVal(curChar)) {
                  children.push(consumeEscapedVal(iter));
                  continue;
                }

                iter.next();
              }

              return {type: "key", start: start, end: iter.pos(), children: children};
            }

            function consumeKeyValSeparator(iter) {
              var start = iter.pos();

              var seenHardSep = false, curChar;
              while ((curChar = iter.peek()) !== null) {
                if (isEOL(curChar)) {
                  break;
                }

                if (isWhitespace(curChar)) {
                  iter.next();
                  continue;
                }

                if (seenHardSep) {
                  break;
                }

                seenHardSep = (curChar === ":" || curChar === "=");
                if (seenHardSep) {
                  iter.next();
                  continue;
                }

                break; // curChar is a non-separtor char
              }

              return {type: "key-value-separator", start: start, end: iter.pos()};
            }

            function startsLineBreak(iter) {
              return iter.peek() === "\\" && isEOL(iter.peek(1));
            }

            function consumeLineBreak(iter) {
              var start = iter.pos();

              iter.next(); // consume \
              if (iter.peek() === "\r") {
                iter.next();
              }
              iter.next(); // consume \n

              var curChar;
              while ((curChar = iter.peek()) !== null) {
                if (isEOL(curChar)) {
                  break;
                }
                if (!isWhitespace(curChar)) {
                  break;
                }

                iter.next();
              }

              return {type: "line-break", start: start, end: iter.pos()};
            }

            function consumeVal(iter) {
              var start = iter.pos(), children = [];

              var curChar;
              while ((curChar = iter.peek()) !== null) {
                if (startsLineBreak(iter)) {
                  children.push(consumeLineBreak(iter));
                  continue;
                }
                if (startsEscapedVal(curChar)) {
                  children.push(consumeEscapedVal(iter));
                  continue;
                }
                if (isEOL(curChar)) {
                  break;
                }

                iter.next();
              }

              return {type: "value", start: start, end: iter.pos(), children: children};
            }

            function consumeKeyVal(iter) {
              return {
                type: "key-value",
                start: iter.pos(),
                children: [
                  consumeKey(iter),
                  consumeKeyValSeparator(iter),
                  consumeVal(iter)
                ],
                end: iter.pos()
              };
            }

            var renderChild = {
              "escaped-value": function(child, text) {
                var type = text.charAt(child.start + 1);

                if (type === "t") {
                  return "\t";
                }
                if (type === "r") {
                  return "\r";
                }
                if (type === "n") {
                  return "\n";
                }
                if (type === "f") {
                  return "\f";
                }
                if (type !== "u") {
                  return type;
                }

                return String.fromCharCode(parseInt(text.substr(child.start + 2, 4), 16));
              },
              "line-break": function(child, text) {
                return "";
              }
            };

            function rangeToBuffer(range, text) {
              var start = range.start, buffer = [];

              for (var i = 0; i < range.children.length; i++) {
                var child = range.children[i];

                buffer.push(text.substring(start, child.start));
                buffer.push(renderChild[child.type](child, text));
                start = child.end;
              }
              buffer.push(text.substring(start, range.end));

              return buffer;
            }

            function rangesToObject(ranges, text) {
              var obj = {};

              for (var i = 0; i < ranges.length; i++) {
                var range = ranges[i];

                if (range.type !== "key-value") {
                  continue;
                }

                var key = rangeToBuffer(range.children[0], text).join("");
                var val = rangeToBuffer(range.children[2], text).join("");
                obj[key] = val;
              }

              return obj;
            }

            function stringToRanges(text) {
              var iter = new Iterator(text), ranges = [];

              var curChar;
              while ((curChar = iter.peek()) !== null) {
                if (isWhitespace(curChar)) {
                  ranges.push(consumeWhiteSpace(iter));
                  continue;
                }
                if (startsComment(curChar)) {
                  ranges.push(consumeComment(iter));
                  continue;
                }
                if (startsKeyVal(curChar)) {
                  ranges.push(consumeKeyVal(iter));
                  continue;
                }

                throw Error("Something crazy happened. text: '" + text + "'; curChar: '" + curChar + "'");
              }

              return ranges;
            }

            function isNewLineRange(range) {
              if (!range) {
                return false;
              }

              if (range.type === "whitespace") {
                return true;
              }

              if (range.type === "literal") {
                return isWhitespace(range.text) && range.text.indexOf("\n") > -1;
              }

              return false;
            }

            function parse(text) {
              text = text.toString();
              var ranges = stringToRanges(text);
              return rangesToObject(ranges, text);
            }

            var jsonFormat = "json";
            var propertiesFormat = "properties";

            return function(options) {

              if (!options || (!angular.isString(options.prefix) || !angular.isString(options.suffix))) {
                throw new Error('Couldn\'t load static files, no prefix or suffix specified!');
              }

              var fileFormat = jsonFormat;
              if (options.fileFormat) {
                fileFormat = options.fileFormat;
              }

              function onSuccess(data) {
                var result = data.data;
                if (fileFormat === propertiesFormat) {
                  result = parse(data.data);
                }

                return result;
              };

              function onError(data) {
                return options.key;
              }

              var url = [
                options.prefix,
                options.key,
                options.suffix
              ].join('');

              return $http.get(url)
                  .then(onSuccess, onError);
            };
          }]);
    }
);
