/*******************************************************************************
 *  Copyright: 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                        and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

if (!Error.prototype.toString || Error.prototype.toString() == "[object Error]")
{
  /**
   * Some browsers (e.g. Internet Explorer) do not support to stringify
   * error objects like other browsers usually do. This feature is added to
   * those browsers.
   *
   * @type member
   * @return {var} TODOC
   */
  Error.prototype.toString = function() {
    return this.message;
  };
}

if (!Array.prototype.indexOf)
{
  /**
   * Returns the first index at which a given element can be found in the array,
   * or <code>-1</code> if it is not present. It compares <code>searchElement</code> to elements of the Array
   * using strict equality (the same method used by the <code>===</code>, or
   * triple-equals, operator).
   *
   * Natively supported in Gecko since version 1.8.
   * http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Objects:Array:indexOf
   *
   * @type member
   * @param searchElement {var} Element to locate in the array.
   * @param fromIndex {Integer} The index at which to begin the search. Defaults to 0, i.e. the whole
   *         array will be searched. If the index is greater than or equal to the length of the array,
   *         <code>-1</code> is returned, i.e. the array will not be searched. If negative, it is taken as the
   *         offset from the end of the array. Note that even when the index is negative, the array is still
   *         searched from front to back. If the calculated index is less than 0, the whole array will be searched.
   * @return {var} TODOC
   */
  Array.prototype.indexOf = function(searchElement, fromIndex)
  {
    if (fromIndex == null) {
      fromIndex = 0;
    } else if (fromIndex < 0) {
      fromIndex = Math.max(0, this.length + fromIndex);
    }

    for (var i=fromIndex; i<this.length; i++)
    {
      if (this[i] === searchElement) {
        return i;
      }
    }

    return -1;
  };
}

if( /iPad|iPhone|iPod/.test( navigator.userAgent ) && /Version\/6/.test( navigator.userAgent ) ) {

// From https://gist.github.com/ronkorving/3755461:
(function (window) {

        // This library re-implements setTimeout, setInterval, clearTimeout, clearInterval for iOS6.
        // iOS6 suffers from a bug that kills timers that are created while a page is scrolling.
        // This library fixes that problem by recreating timers after scrolling finishes (with interval correction).
    // This code is free to use by anyone (MIT, blabla).
    // Author: rkorving@wizcorp.jp

        var timeouts = {};
        var intervals = {};
        var orgSetTimeout = window.setTimeout;
        var orgSetInterval = window.setInterval;
        var orgClearTimeout = window.clearTimeout;
        var orgClearInterval = window.clearInterval;


        function createTimer(set, map, args) {
                var id, cb = args[0], repeat = (set === orgSetInterval);

                function callback() {
                        if (cb) {
                                cb.apply(window, arguments);

                                if (!repeat) {
                                        delete map[id];
                                        cb = null;
                                }
                        }
                }

                args[0] = callback;

                id = set.apply(window, args);

                map[id] = { args: args, created: Date.now(), cb: cb, id: id };

                return id;
        }


        function resetTimer(set, clear, map, virtualId ) {
                var timer = map[virtualId];

                if (!timer) {
                        return;
                }

                var repeat = (set === orgSetInterval);

                // cleanup

                clear(timer.id);

                // reduce the interval (arg 1 in the args array)

                if (!repeat) {
                        var interval = timer.args[1];

                        var reduction = Date.now() - timer.created;
                        if (reduction < 0) {
                                reduction = 0;
                        }

                        interval -= reduction;
                        if (interval < 0) {
                                interval = 0;
                        }

                        timer.args[1] = interval;
                }

                // recreate

                function callback() {
                        if (timer.cb) {
                                timer.cb.apply(window, arguments);
                                if (!repeat) {
                                        delete map[virtualId];
                                        timer.cb = null;
                                }
                        }
                }

                timer.args[0] = callback;
                timer.created = Date.now();
                timer.id = set.apply(window, timer.args);
        }


        window.setTimeout = function () {
                return createTimer(orgSetTimeout, timeouts, arguments);
        };


        window.setInterval = function () {
                return createTimer(orgSetInterval, intervals, arguments);
        };

        window.clearTimeout = function (id) {
                var timer = timeouts[id];

                if (timer) {
                        delete timeouts[id];
                        orgClearTimeout(timer.id);
                }
        };

        window.clearInterval = function (id) {
                var timer = intervals[id];

                if (timer) {
                        delete intervals[id];
                        orgClearInterval(timer.id);
                }
        };

        window.addEventListener('scroll', function () {
                // recreate the timers using adjusted intervals
                // we cannot know how long the scroll-freeze lasted, so we cannot take that into account

                var virtualId;

                for (virtualId in timeouts) {
                        resetTimer(orgSetTimeout, orgClearTimeout, timeouts, virtualId);
                }

                for (virtualId in intervals) {
                        resetTimer(orgSetInterval, orgClearInterval, intervals, virtualId);
                }
        });

}(window));

}
