/*!
 * Copyright 2010 - 2019 Hitachi Vantara. All rights reserved.
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
  "./Base",
  "../util/object",
  "../util/error",
  "./UserError"
], function(Base, O, error, UserError) {

  "use strict";

  // NOTE: Not currently being used.

  return Base.extend("pentaho.lang.Event", /** @lends pentaho.lang.Event# */{
    /**
     * @classDesc The `Event` class is the base class of a certain kind of event objects that can be
     * emitted by [event sources]{@link pentaho.lang.EventSource}.
     *
     * The source of an event is the object that emits it and
     * is given by the event's [source]{@link pentaho.lang.Event#source} property.
     *
     * The type of an event is given by its [type]{@link pentaho.lang.Event#type} property.
     *
     * ##### Event Cancellation
     *
     * Certain types of events are used to signal that an _action_ is about to execute
     * (or that a phase of an already executing action is about to start).
     * When the execution of the action can be canceled by the event listeners,
     * the event is said to be _cancelable_.
     * That characteristic is exposed by the [isCancelable]{@link pentaho.lang.Event#isCancelable} property.
     *
     * When an event is canceled, the listeners of unprocessed registrations are not notified.
     *
     * To cancel an event, call its [cancel]{@link pentaho.lang.Event#cancel} method.
     * To find out if an event has been canceled, read the [isCanceled]{@link pentaho.lang.Event#isCanceled} property.
     *
     * ##### Persistable Events
     *
     * Certain types of events are emitted so frequently that it
     * makes it highly beneficial to reuse event objects.
     * To safely use an event object beyond its emission,
     * a cloned event object must be obtained,
     * through [clone]{@link pentaho.lang.Event#clone}.
     *
     * @name Event
     * @memberOf pentaho.lang
     * @class
     * @amd pentaho/lang/Event
     * @private
     *
     * @description Creates an event of a given type, source, and ability to be canceled.
     * @constructor
     * @param {nonEmptyString} type - The type of the event.
     * @param {pentaho.lang.IEventSource} source - The object where the event is emitted.
     * @param {?boolean} [cancelable=false] - Indicates if the event can be canceled.
     */
    constructor: function(type, source, cancelable) {
      if(!type) throw error.argRequired("type");
      if(!source) throw error.argRequired("source");

      this.__type = type;
      this.__source = source;
      this.__cancelable = !!cancelable;
    },

    __cancelReason: null,
    __isCanceled: false,

    /**
     * Gets the type of the event.
     *
     * @type {nonEmptyString}
     * @readonly
     */
    get type() {
      return this.__type;
    },

    /**
     * Gets the source of the event.
     *
     * @type {pentaho.lang.IEventSource}
     * @readonly
     */
    get source() {
      return this.__source;
    },

    /**
     * Gets a value that indicates if the event can be canceled.
     *
     * @type {boolean}
     * @readonly
     */
    get isCancelable() {
      return this.__cancelable;
    },

    /**
     * Gets the reason why the event was canceled, if any, or `null`.
     *
     * @type {Error}
     * @readonly
     */
    get cancelReason() {
      return this.__cancelReason;
    },

    /**
     * Cancels the event.
     *
     * This method has no effect if the event is not cancelable or has already been canceled.
     *
     * @param {string|Error} [reason="canceled"] - The reason why the event is being canceled.
     *
     * @see pentaho.lang.Event#isCancelable
     * @see pentaho.lang.Event#isCanceled
     * @see pentaho.lang.Event#cancelReason
     */
    cancel: function(reason) {
      if(this.__cancelable && !this.__isCanceled) {
        if(!reason) reason = "canceled";

        if(typeof reason === "string") {
          reason = new UserError(reason);
        } else if(!(reason instanceof Error)) {
          throw error.argInvalidType("reason", ["string", "Error"], typeof reason);
        }

        this.__cancelReason = reason;
        this.__isCanceled = true;
      }
    },

    /**
     * Gets a value that indicates if the event has been canceled.
     *
     * @type {boolean}
     * @readonly
     */
    get isCanceled() {
      return this.__isCanceled;
    },

    /**
     * Creates a clone of the event object.
     *
     * @return {pentaho.lang.Event} The cloned event object.
     */
    clone: function() {
      var proto = Object.getPrototypeOf(this);

      var clone = Object.create(proto);
      for(var name in this) {
        if(this.hasOwnProperty(name)) {
          var desc = O.getPropertyDescriptor(this, name);
          Object.defineProperty(clone, name, desc);
        }
      }

      return clone;
    }
  });
});
