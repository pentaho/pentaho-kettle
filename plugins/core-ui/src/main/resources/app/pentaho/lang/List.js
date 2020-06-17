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
  "./Base"
], function(Base) {

  var baseProto = Base.Array.prototype;

  // TODO: This class has several undocumented methods.

  return Base.Array.extend("pentaho.lang.List", /** @lends  pentaho.lang.List# */{
    /**
     * @classdesc The `List` class is an abstract base class for typed arrays.
     *
     * Elements of a list must implement the {@link pentaho.lang.IListElement} interface.
     *
     * @description Initializes a list instance.
     *
     * Note that because a `List` is a sub-class of `Array`,
     * the `new` operator cannot be used to create instances (at least up to ECMAScript version 5).
     * Instead, instances of `List` are actually initial instances of `Array`
     * whose prototype is then changed to be that of `List`.
     * In other words, `List` is an "initialization" constructor
     * (see {@link pentaho.lang.ISpecifiable} for more information
     *  on these concepts.
     *
     * Concrete `List` sub-classes should provide a static `to` method
     * to help in their construction.
     *
     * @class
     * @abstract
     * @name List
     * @memberOf pentaho.lang
     * @extends pentaho.lang.Base.Array
     * @amd pentaho/lang/List
     * @param {?object} [keyArgs] The keyword arguments.
     *
     * These are not used directly by the `List` class
     * but are passed-through to the methods that handle
     * the initialization of each list element.
     */
    constructor: function(keyArgs) {
      this._addMany(this, keyArgs);
    },

    // Optional hook methods
    // _adding(elem, index, keyArgs) -> replacement elem or undefined
    // call base in this one or call _cast

    // _added(elem, index, keyArgs)
    _added: null,

    // _replacing(elem, index, elem0, keyArgs) -> replacement elem or undefined
    // _replacing: null,
    // call base or call _cast

    // _replaced(elem, index, elem0, keyArgs)
    _replaced: null,

    // abstract
    /**
     * Gets the constructor function of the elements held by this list.
     *
     * This class must implement the {@link pentaho.lang.IListElement} interface.
     *
     * @type {Class}
     * @readonly
     */
    elemClass: null,

    /**
     * Gets a common name for the elements held by this list.
     *
     * The default implementation returns the value of
     * {@link pentaho.lang.IListElement#elemName} of
     * {@link pentaho.lang.List#elemClass}.
     *
     * @return {string} The common name of the elements.
     * @protected
     */
    _getElemName: function() {
      return this.elemClass.prototype.elemName;
    },

    /**
     * The length of the list.
     *
     * @name pentaho.lang.List#length
     * @readonly
     * @type {number}
     */

    /**
     * Appends elements to the list and returns its new length.
     *
     * The values specified in `elems` are converted to the list elements' class
     * before actually being added to it.
     *
     * This method adds elements to the list using default options.
     * Use one of
     * {@link pentaho.lang.List#add} or
     * {@link pentaho.lang.List#addMany}
     * to be able to specify non-default options (keyword arguments).
     *
     * @param {...*} elems The elements to add to the list.
     * @return {number} The new length of the list.
     */
    push: function() {
      return this._addMany(arguments);
    },

    /**
     * Appends elements to the list and returns its new length.
     *
     * The values specified in `elems` are converted to the list elements' class
     * before actually being added to it.
     *
     * This method allows adding elements to the list using custom options (keyword arguments).
     * Contrast with method {@link pentaho.lang.List#push} which
     * adds elements using default options.
     *
     * @param {Array} elems An array of elements to add to the list.
     * @param {?object} [keyArgs] The keyword arguments.
     *
     * These are not used directly by the `List` class
     * but are passed-through to the methods that handle
     * the initialization of each list element.
     *
     * @return {number} The new length of the list.
     */
    addMany: function(elems, keyArgs) {
      return this._addMany(elems, keyArgs);
    },

    /**
     * Appends an element to the list and returns it.
     *
     * The value specified in argument `elem` is converted to the list elements' class
     * before actually being added to it.
     *
     * @param {*} elem An element or a value convertible to one.
     * @param {?object} [keyArgs] The keyword arguments.
     *
     * These are not used directly by the `List` class
     * but are passed-through to the methods that handle
     * the initialization of list elements.
     *
     * @return {pentaho.lang.IListElement} The added list element.
     */
    add: function(elem, keyArgs) {
      return this.insert(elem, this.length, keyArgs);
    },

    replace: function(elem, at, keyArgs) {
      var elem0 = this[at];
      var elem2 = this._replacing(elem, at, elem0, keyArgs);
      if(elem2 !== undefined && (elem0 !== elem2)) {
        this[at] = elem2;
        if(this._replaced) this._replaced(elem2, at, elem0, keyArgs);
      }

      return elem2;
    },

    insert: function(elem, at, keyArgs) {
      var elem2 = this._adding(elem, at, keyArgs);
      if(elem2 !== undefined) {
        baseProto.splice.call(this, at, 0, elem2);
        if(this._added) this._added(elem2, at, keyArgs);
      }

      return elem2;
    },

    _adding: function(elem, index, keyArgs) {
      return this._cast(elem, index, keyArgs);
    },

    _replacing: function(elem, index, keyArgs) {
      return this._cast(elem, index, keyArgs);
    },

    _cast: function(elem, index, keyArgs) {
      return this.elemClass ? this.elemClass.to(elem, keyArgs) : elem;
    },

    _addMany: function(elems, keyArgs) {
      var isReplay = elems === this;
      var LE = elems.length;
      if(!LE) return this.length;

      var added = this._added;
      var i = 0;
      var at = isReplay ? 0 : this.length;
      while(i < LE) {
        var elem = elems[i];
        var elem2 = this._adding(elem, at, keyArgs);
        if(elem2 === undefined) {
          // Not added afterall
          if(isReplay) {
            // Remove from `at`
            this.splice(at, 1);
            LE--;
            continue;
          }
        } else {
          if(isReplay) {
            if(elem !== elem2) this[at] = elem2;
          } else {
            baseProto.push.call(this, elem2);
          }
          if(added) added.call(this, elem2, at, keyArgs);
        }
        at++;
        i++;
      }

      return at;
    },

    copyTo: function(list) {
      baseProto.push.apply(list, this);
    },

    // region ISpecifiable implementation
    /**
     * Creates a specification of this list.
     *
     * A list specification is an array containing the specifications of each of its elements.
     *
     * If the element's class does not implement {@link pentaho.lang.ISpecifiable},
     * each element is assumed to be its own specification.
     *
     * @return {Array} The list specification.
     */
    toSpec: function() {
      return this.map(function(elem) { return elem.toSpec ? elem.toSpec() : elem; });
    }
    // endregion
  });
});
