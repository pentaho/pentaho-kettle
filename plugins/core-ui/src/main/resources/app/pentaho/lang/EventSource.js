/*!
 * Copyright 2010 - 2017 Hitachi Vantara. All rights reserved.
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
  "module",
  "./Base",
  "./Event",
  "../module/metaService",
  "../util/error",
  "../util/object",
  "../util/fun",
  "../util/logger"
], function(module, Base, Event, moduleMetaService, error, O, F, logger) {

  "use strict";

  /* eslint new-cap: 0, dot-notation: 0 */

  var O_hasOwn = Object.prototype.hasOwnProperty;
  var keyArgsEmitEventUnsafe = {isCanceled: __event_isCanceled, errorHandler: null};
  var keyArgsEmitEventSafe = {isCanceled: __event_isCanceled, errorHandler: __defaultErrorHandlerLog};

  // region EventRegistrationHandle
  /**
   * @classDesc The `EventRegistrationHandle` class handles creating the `remove` alias,
   * for the `dispose` method.
   *
   * @alias EventRegistrationHandle
   * @memberOf pentaho.lang
   * @class
   * @implements {pentaho.lang.IEventRegistrationHandle}
   * @private
   *
   * @constructor
   * @description Creates a registration handle with a given dispose method.
   * @param {function} dispose - The dispose method.
   */
  function pentaho_lang_IEventRegistrationHandle(dispose) {
    this.dispose = dispose;
  }

  /**
   * Removes the registration.
   * @alias remove
   * @memberOf pentaho.lang.EventRegistrationHandle#
   */
  pentaho_lang_IEventRegistrationHandle.prototype.remove = function() {
    this.dispose();
  };
  // endregion

  // region IEventObserverRegistration
  /**
   * Holds registration information about an observer.
   *
   * @name pentaho.lang.IEventObserverRegistration
   * @interface
   * @property {pentaho.lang.IEventObserver} observer - The observer.
   * @property {number} priority - The priority of the observer.
   * @property {number} index - The index of the observer in the event's observers list.
   * @private
   */
  // endregion

  /**
   * @classDesc The `EventSource` class is an implementation [IEventSource]{@link pentaho.lang.IEventSource}
   * that can be used as **mixin** class for classes that emit events.
   *
   * The class publicly exposes the `IEventSource` interface, allowing the registration and
   * unregistration of event listeners/observers.
   * The ability to emit events is, however, only exposed via the protected interface, through the methods:
   * [_emit]{@link pentaho.lang.EventSource#_emit},
   * [_emitSafe]{@link pentaho.lang.EventSource#_emitSafe},
   * [_emitGeneric]{@link pentaho.lang.EventSource#_emitGeneric} and
   * [_emitGenericAllAsync]{@link pentaho.lang.EventSource#_emitGenericAllAsync}.
   * The methods `_emit` and `_emitSafe` are to be used with events of the [Event]{@link pentaho.lang.Event} type.
   * The methods `_emitGeneric` and `_emitGenericAllAsync` can be used with any event type and
   * expose more control options.
   *
   * To ascertain whether any listeners are registered for a certain event type and, optionally, phase,
   * use [_hasListeners]{@link pentaho.lang.EventSource#_hasListeners}.
   *
   * @name EventSource
   * @memberOf pentaho.lang
   * @class
   * @implements {pentaho.lang.IEventSource}
   * @amd pentaho/lang/EventSource
   *
   * @description This class was not designed to be constructed directly.
   * It was designed to be used as a **mixin**.
   * @constructor
   */

  return Base.extend(module.id, /** @lends pentaho.lang.EventSource# */{

    // region Registration
    /**
     * The registry of event observers by event type.
     *
     * Listeners of unstructured events are registered as observers of the special `"__"` phase.
     *
     * @type {Map.<nonEmptyString, Array.<pentaho.lang.IEventObserverRegistration>>}
     * @private
     */
    __observersRegistry: null,

    on: function(type, observer, keyArgs) {
      if(!type) throw error.argRequired("type");
      if(!observer) throw error.argRequired("observer");

      var eventTypes = __parseEventTypes(type);
      if(eventTypes && eventTypes.length) {

        var priority = (keyArgs && keyArgs.priority) || 0;
        var isCritical = !!(keyArgs && keyArgs.isCritical);

        if(F.is(observer)) observer = {__: observer};

        /** @type pentaho.lang.IEventRegistrationHandle[] */
        var handles = eventTypes.map(function(type) {
          return __registerOne.call(this, type, observer, priority, isCritical);
        }, this);

        return handles.length > 1
          ? new pentaho_lang_IEventRegistrationHandle(__disposeHandles.bind(this, handles))
          : handles[0];
      }

      return null;
    },

    off: function(typeOrHandle, observer) {
      if(!typeOrHandle) throw error.argRequired("typeOrHandle");

      if(typeOrHandle instanceof pentaho_lang_IEventRegistrationHandle) {
        // This is just syntax sugar, so let dispose from any source.
        typeOrHandle.dispose();
        return;
      }

      if(!observer) throw error.argRequired("observer");

      var types = __parseEventTypes(typeOrHandle);
      if(types && types.length) {

        var find = F.is(observer) ? __findUnstructuredObserverRegistration : __findObserverRegistration;

        types.forEach(function(type) {

          // Resolve alias
          var module = moduleMetaService.get(type);
          if(module !== null) {
            type = module.id;
          }

          var observerRegistration = find.call(this, type, observer);
          if(observerRegistration) {
            __unregisterOne.call(this, type, observerRegistration);
          }
        }, this);
      }
    },

    /**
     * Determines if there are any registrations for a given event type and, optionally, phase.
     *
     * This method can be used to avoid creating expensive event objects
     * for an event type and, optionally, phase, that don't have registrations.
     *
     * @example
     *
     * if(this._hasListeners("click")) {
     *
     *   var event = new Event("click", this, true);
     *
     *   if(this._emit(event)) {
     *     // ...
     *   }
     * }
     *
     * @example
     *
     * if(this._hasListeners("select")) {
     *
     *   var event = new Event("select");
     *
     *   if(this._emitGeneric(this, [event], "select", "will")) {
     *
     *     // Select ...
     *
     *     this._emitGeneric(this, [event], "select", "finally");
     *   }
     * }
     *
     * @param {nonEmptyString} type - The type of the event.
     * @param {?nonEmptyString} [phase] - The phase of a structured event.
     * For unstructured events don't specify this argument.
     * For structured events, if this argument is not specified,
     * the result will be `true` if there are any listeners, for any of the phases.
     *
     * @return {boolean} `true` if the event has any listeners for the given event type and phase; `false`, otherwise.
     *
     * @protected
     */
    _hasListeners: function(type, phase) {

      // Resolve alias
      var module = moduleMetaService.get(type);
      if(module !== null) {
        type = module.id;
      }

      var queue = O.getOwn(this.__observersRegistry, type, null);
      if(queue !== null) {
        if(!phase) {
          return true;
        }

        // Find at least one observer with the desired phase.
        var i = -1;
        var L = queue.length;
        while(++i < L) {
          if(O_hasOwn.call(queue[i].observer, phase)) {
            return true;
          }
        }
      }

      return false;
    },
    // endregion

    // region Emission
    /**
     * Emits an unstructured event and returns it, unless it was canceled.
     *
     * When this method is called, the listeners of existing registrations are notified
     * synchronously, by priority order and then registration order,
     * until either the event is canceled or all of the listeners have been notified.
     *
     * It is safe to register or unregister to/from an event type while it is being emitted.
     * However, changes are only taken into account in subsequent emissions.
     *
     * If a listener function throws an error, the event processing is interrupted.
     * No more registrations are processed and the error is passed to the caller.
     *
     * @see pentaho.lang.EventSource#_emitSafe
     * @see pentaho.lang.EventSource#_emitGeneric
     *
     * @param {pentaho.lang.Event} event - The event object.
     *
     * @return {?pentaho.lang.Event} The given event object or `null`, when canceled.
     *
     * @protected
     */
    _emit: function(event) {
      if(!event) throw error.argRequired("event");
      if(!(event instanceof Event)) throw error.argInvalidType("event", "pentaho.type.Event");

      return this._emitGeneric(this, [event], event.type, null, keyArgsEmitEventUnsafe) ? event : null;
    },

    /**
     * Variation of the [_emit]{@link pentaho.lang.EventSource#_emit} method in which
     * errors thrown by event listeners are caught and logged.
     *
     * If an event listener throws an error, the following event listeners are still processed.
     *
     * @see pentaho.lang.EventSource#_emit
     * @see pentaho.lang.EventSource#_emitGeneric
     *
     * @param {pentaho.lang.Event} event - The event object.
     *
     * @return {?pentaho.lang.Event} The given event object or `null`, when canceled.
     *
     * @protected
     */
    _emitSafe: function(event) {
      if(!event) throw error.argRequired("event");
      if(!(event instanceof Event)) throw error.argInvalidType("event", "pentaho.type.Event");

      return this._emitGeneric(this, [event], event.type, null, keyArgsEmitEventSafe) ? event : null;
    },

    /**
     * Emits an event given an arbitrary payload object, its type and phase.
     * Returns the event payload object, unless the event is canceled.
     *
     * @param {object} source - The `this` value of listener functions.
     * @param {Array} eventArgs - The arguments of listener functions.
     * @param {nonEmptyString} type - The type of the event.
     * @param {?nonEmptyString} [phase] - The phase of the event. For unstructured events don't specify this argument
     * (or specify a {@link Nully} value).
     *
     * @param {?object} [keyArgs] - The keyword arguments' object.
     * @param {function(...*):boolean} [keyArgs.isCanceled] - A predicate that indicates if the given event arguments
     * are in a canceled state. Its `this` value is the value of `source`.
     * @param {function(*, Array, nonEmptyString, nonEmptyString)} [keyArgs.errorHandler] -
     * When specified with a `null` value,
     * no error handling is performed and errors thrown by listeners are thrown back to this method's caller.
     * When unspecified or specified as `undefined`, defaults to a function that simply logs the listener errors,
     * and lets execution continue to the following listeners.
     * The function arguments are: the error, the eventArgs, the event type and the event phase.
     * Its `this` value is the value of `source`.
     * @param {function} [keyArgs.interceptor=null] A function which is called for each event listener function,
     * with the arguments `listener`, `source`, `eventArgs` and the index of the listener.
     *
     * @return {boolean} `false` when the event is canceled; `true`, otherwise.
     *
     * @protected
     */
    _emitGeneric: function(source, eventArgs, type, phase, keyArgs) {

      if(!source) throw error.argRequired("source");
      if(!eventArgs) throw error.argRequired("eventArgs");
      if(!type) throw error.argRequired("type");

      var isCanceled;
      if((isCanceled = keyArgs && keyArgs.isCanceled) && isCanceled.apply(source, eventArgs)) {
        return false;
      }

      // Resolve alias
      var module = moduleMetaService.get(type);
      if(module !== null) {
        type = module.id;
      }

      var queue;
      if((queue = O.getOwn(this.__observersRegistry, type, null)) === null) {
        return true;
      }

      var phaseEf;
      if(!phase || phase === "__") {
        phase = null;
        phaseEf = "__";
      } else {
        phaseEf = phase;
      }

      // Use `null` to force no error handling.
      var errorHandler = keyArgs && keyArgs.errorHandler;
      if(errorHandler === undefined) {
        errorHandler = __defaultErrorHandlerLog;
      }

      var interceptor = (keyArgs && keyArgs.interceptor) || null;

      // ---

      queue.emittingLevel++;

      try {
        var i = queue.length;
        var listener;

        if(errorHandler) {

          while(i--) if((listener = queue[i].observer[phaseEf])) {

            try {
              if(interceptor === null) {
                listener.apply(source, eventArgs);
              } else {
                interceptor(listener, source, eventArgs, i);
              }
            } catch(ex) {

              if(queue[i].isCritical) {
                throw ex;
              }

              errorHandler.call(source, ex, eventArgs, type, phase);
            }

            // error handler can decide to cancel the event.
            if(isCanceled && isCanceled.apply(source, eventArgs)) return false;
          }

        } else {

          while(i--) if((listener = queue[i].observer[phaseEf])) {

            if(interceptor === null) {
              listener.apply(source, eventArgs);
            } else {
              interceptor(listener, source, eventArgs, i);
            }

            if(isCanceled && isCanceled.apply(source, eventArgs)) return false;
          }

        }
      } finally {
        queue.emittingLevel--;
      }

      return true;
    },

    /**
     * Emits an event asynchronously, given an arbitrary payload object, its type and phase,
     * and succeeding if every listener succeeds.
     *
     * Listeners are called in parallel.
     *
     * Returns a promise that is fulfilled or rejected with the event payload object.
     * If any listener throws or rejects, the returned promise is rejected as well.
     *
     * @param {object} source - The `this` value of listener functions.
     * @param {Array} eventArgs - The arguments of listener functions.
     * @param {nonEmptyString} type - The type of the event.
     * @param {?nonEmptyString} [phase] - The phase of the event. For unstructured events don't specify this argument
     * (or specify a {@link Nully} value).
     *
     * @param {?object} [keyArgs] - The keyword arguments' object.
     * @param {function(...*):boolean} [keyArgs.isCanceled] - A predicate that indicates if the given event arguments
     * are in a canceled state. Its `this` value is the value of `source`.
     * @param {function(...*):*} [keyArgs.getCancellationReason] - A function that given the event arguments
     * returns its a cancellation reason, usually an {@link Error}.  Its `this` value is the value of `source`.
     * @param {function(*, Array, nonEmptyString, nonEmptyString) : Promise} [keyArgs.errorHandler] -
     * When specified with a `null` value, no error handling is performed.
     * Errors thrown by, or promises rejected by,
     * any listeners cause the whole event to be rejected.
     *
     * When unspecified or specified as `undefined`,
     * defaults to a function that simply logs any listener errors,
     * yet always succeeding.
     *
     * The function arguments are: the error, the event, the event type and the event phase.
     *
     * @return {Promise.<object>} A promise.
     * When fulfilled, it is with the value `undefined`.
     * When rejected due to a thrown error, the rejection reason is that error.
     * When explicitly rejected by the error handler, the given rejection reason is preserved.
     * When rejected due to a cancellation, the rejection reason is the cancellation reason, if any.
     *
     * @protected
     */
    _emitGenericAllAsync: function(source, eventArgs, type, phase, keyArgs) {

      if(!source) throw error.argRequired("source");
      if(!eventArgs) throw error.argRequired("eventArgs");
      if(!type) throw error.argRequired("type");

      var isCanceled = keyArgs && keyArgs.isCanceled;
      var getCancellationReason = keyArgs && keyArgs.getCancellationReason;

      if(isCanceled && isCanceled.apply(source, eventArgs)) {
        return getCancellationReason != null
          ? Promise.reject(getCancellationReason.apply(source, eventArgs))
          : Promise.reject();
      }

      // Resolve alias
      var module = moduleMetaService.get(type);
      if(module !== null) {
        type = module.id;
      }

      var queue;
      if((queue = O.getOwn(this.__observersRegistry, type, null)) === null) {
        return Promise.resolve();
      }

      var phaseEf;
      if(!phase || phase === "__") {
        phase = null;
        phaseEf = "__";
      } else {
        phaseEf = phase;
      }

      // Use `null` to force no error handling.
      var errorHandler = keyArgs && keyArgs.errorHandler;
      if(errorHandler === undefined) {
        errorHandler = __defaultErrorHandlerLog;
      }

      // ----

      queue.emittingLevel++;

      var promises = [];
      var i = queue.length;
      var listener;
      while(i--) if((listener = queue[i].observer[phaseEf]) != null) {
        // Calls listener, synchronously,
        // so `queue` is externally observable while in this loop.
        promises.push(emitOne(listener));
      }

      queue.emittingLevel--;

      return Promise.all(promises).then(function() {});

      function emitOne(listener) {
        // Call listener wrapped so that any thrown errors are converted into rejections.
        var promiseOne = new Promise(function(resolveInner) {
          resolveInner(listener.apply(source, eventArgs));
        });

        if(errorHandler !== null) {
          // Let the errorHandler resolve the failure.
          promiseOne = promiseOne["catch"](function(ex) {
            return errorHandler.call(source, ex, eventArgs, type, phase);
          });
        }
        // else, don't know how to affect `event` to mark it failed.
        // Just let the rejection pass-through.

        if(isCanceled != null) {

          // error handler may cancel the event...

          promiseOne = promiseOne.then(function() {
            if(isCanceled.apply(source, eventArgs)) {
              return getCancellationReason != null
                ? Promise.reject(getCancellationReason.apply(source, eventArgs))
                : Promise.reject();
            }
          });
        }

        return promiseOne;
      }
    }
    // endregion
  });

  /**
   * Gets or creates the queue of observer registrations for a given event type.
   *
   * @this pentaho.lang.EventSource
   *
   * @param {nonEmptyString} type - The event type.
   * @return {Array.<pentaho.lang.IEventObserverRegistration>} The array of event observer registrations.
   *
   * @private
   */
  function __getObserversQueueOf(type) {
    var registry = this.__observersRegistry || (this.__observersRegistry = {});
    return registry[type] || (registry[type] = __createObserversQueue());
  }

  /**
   * Creates a queue of observer registrations.
   *
   * @memberOf pentaho.lang.EventSource~
   * @return {Array.<pentaho.lang.IEventObserverRegistration>} An empty queue of event observer registrations.
   *
   * @private
   */
  function __createObserversQueue() {
    var queue = [];
    queue.emittingLevel = 0;
    return queue;
  }

  /**
   * Register an observer given its event type and priority.
   *
   * @memberOf pentaho.lang.EventSource#
   *
   * @param {nonEmptyString} type - The event tyoe.
   * @param {pentaho.lang.IEventObserver} observer - The event observer.
   * @param {number} priority - The listening priority.
   * @param {boolean} isCritical - Indicates that exceptions in this listener should abort the execution.
   *
   * @return {pentaho.lang.IEventRegistrationHandle} An event registration handle that can be used for later removal.
   *
   * @private
   */
  function __registerOne(type, observer, priority, isCritical) {

    // Resolve alias
    var module = moduleMetaService.get(type);
    if(module !== null) {
      type = module.id;
    }

    var queue = __getObserversQueueOf.call(this, type, /* create: */true);

    var i = queue.length;

    /** @type ?pentaho.lang.IEventObserverRegistration */
    var observerRegistration;
    while(i-- && (observerRegistration = queue[i]).priority >= priority) {
      // Will be shifted to the right one position, with the splice operation, below.
      observerRegistration.index = i + 1;
    }

    // `i` has the first position (from end) where priority is lower.
    // Add to its right.
    i++;

    // Can change the queue directly? Or need to copy it?
    if(queue.emittingLevel) {
      // Use "copy on write". Replace by a fresh copy to not affect current iteration.
      this.__observersRegistry[type] = queue = queue.slice();
      queue.emittingLevel = 0;
    }

    observerRegistration = {
      index: i,
      priority: priority,
      observer: observer,
      isCritical: isCritical
    };

    // Insert at index `i`.
    queue.splice(i, 0, observerRegistration);

    return new pentaho_lang_IEventRegistrationHandle(__unregisterOne.bind(this, type, observerRegistration));
  }

  /**
   * Removes an event observer registration.
   *
   * @memberOf pentaho.lang.EventSource#
   *
   * @param {nonEmptyString} type - The event type.
   * @param {pentaho.lang.IEventObserverRegistration} observerRegistration - The event observer registration.
   *
   * @private
   */
  function __unregisterOne(type, observerRegistration) {

    var queue = this.__observersRegistry[type];
    var i = observerRegistration.index;

    // Can change the queue directly? Or need to copy it?
    if(queue.emittingLevel) {
      // Use "copy on write". Replace by a fresh copy to not affect current iteration.
      this.__observersRegistry[type] = queue = queue.slice();
      queue.emittingLevel = 0;
    }

    // Remove index `i`
    queue.splice(i, 1);

    // Update indexes of any observers to the right.
    var L = queue.length;
    if(L) {
      while(i < L) {
        queue[i].index = i;
        i++;
      }
    } else {
      // Was last observer registration.
      // Remove, as _hasListeners depends on this.
      delete this.__observersRegistry[type];
    }
  }

  /**
   * Disposes a given array of event registration handles.
   *
   * @memberOf pentaho.lang.EventSource~
   *
   * @param {Array.<pentaho.lang.IEventRegistrationHandle>} handles - The array of event registration handles.
   *
   * @private
   */
  function __disposeHandles(handles) {
    for(var i = 0, L = handles.length; i !== L; ++i) {
      handles[i].dispose();
    }
  }

  /**
   * Finds the first unstructured event observer registration for the given event type and listener.
   *
   * @memberOf pentaho.lang.EventSource#
   *
   * @param {nonEmptyString} type - The event type.
   * @param {function} listener - The event listener.
   *
   * @return {pentaho.lang.IEventObserverRegistration} The event registration, if any; or `null`, otherwise.
   *
   * @private
   */
  function __findUnstructuredObserverRegistration(type, listener) {
    var queue = O.getOwn(this.__observersRegistry, type);
    if(queue) {
      var i = -1;
      var L = queue.length;
      while(++i < L) if(queue[i].observer.__ === listener) return queue[i];
    }

    return null;
  }

  /**
   * Finds the first structured event observer registration for the given event type and observer.
   *
   * @memberOf pentaho.lang.EventSource#
   *
   * @param {nonEmptyString} type - The event type.
   * @param {pentaho.lang.IEventObserver} observer - The event observer.
   *
   * @return {pentaho.lang.IEventObserverRegistration} The event registration, if any; or `null`, otherwise.
   *
   * @private
   */
  function __findObserverRegistration(type, observer) {
    var queue = O.getOwn(this.__observersRegistry, type);
    if(queue) {
      var i = -1;
      var L = queue.length;
      while(++i < L) if(queue[i].observer === observer) return queue[i];
    }

    return null;
  }

  /**
   * Parses the given event type specification.
   *
   * When a string, multiple event types can be specified separated by a comma, `,`.
   *
   * @memberOf pentaho.lang.EventSource~
   *
   * @param {nonEmptyString|nonEmptyString[]} type - The event type specification.
   *
   * @return {nonEmptyString[]} An array of event types.
   *
   * @private
   */
  function __parseEventTypes(type) {

    if(type instanceof Array) {
      // Allow an array of event types.
      return type;
    }

    if(type.indexOf(",") > -1) {
      // Allow comma delimited event types.
      // Already eats spaces.
      return type.split(/\s*,\s*/);
    }

    return [type];
  }

  /**
   * Determines if a given event object is canceled.
   *
   * @memberOf pentaho.lang.EventSource~
   *
   * @param {pentaho.lang.Event} event - The event object.
   *
   * @return {boolean} `true` if it is canceled; `false`, otherwise.
   *
   * @private
   */
  function __event_isCanceled(event) {
    return event.isCanceled;
  }

  // TODO: check log level!

  /**
   * Logs an error thrown by an event listener.
   *
   * The `this` value is the source of the event.
   *
   * @memberOf pentaho.lang.EventSource~
   *
   * @param {*} error - The thrown value.
   * @param {Array} eventArgs - The arguments of listener functions.
   * @param {nonEmptyString} type - The event type.
   * @param {nonEmptyString} phase - The event phase.
   *
   * @private
   */
  function __defaultErrorHandlerLog(error, eventArgs, type, phase) {

    var eventTypeId = type + (phase !== "__" ? (":" + phase) : "");
    var errorDesc = error ? (" Error: " + error.message) : "";

    logger.log("Event listener of '" + eventTypeId + "' failed." + errorDesc);
  }
});
