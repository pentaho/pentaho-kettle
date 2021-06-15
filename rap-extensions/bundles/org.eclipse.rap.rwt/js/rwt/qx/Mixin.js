/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/


/**
 * This class is used to define mixins (similar to mixins in Ruby).
 *
 * Mixins are collections of code and variables, which can be merged into
 * other classes. They are similar to classes but don't support inheritance.
 *
 * See the description of the {@link #define} method how a mixin is defined.
 */
rwt.qx.Class.define( "rwt.qx.Mixin", {

  statics : {

    /**
     * Defines a new mixin.
     *
     * @param name {String} name of the mixin
     * @param config {Map} Mixin definition structure. The configuration map has the
     *   following keys:
     *   - construct {Function} An optional mixin constructor. It is called on instantiation each
     *         class including this mixin. The constructor takes no parameters.
     *   - destruct {Function} An optional mixin destructor.
     *   - include {Mixin[]} Array of mixins, which will be merged into the mixin.
     *   - statics {Map} Map of statics of the mixin. The statics will not get copied into the
     *         target class. They remain acceccible from the mixin. This is the same behaviour as
     *         statics in interfaces ({@link qx.Interface#define}).
     *   - members {Map} Map of members of the mixin.
     *   - properties {Map} Map of property definitions.
     */
    define : function( name, config ) {
      var mixin = config.statics ? config.statics : {};
      mixin.$$constructor = config.construct;
      mixin.$$properties = config.properties;
      mixin.$$members = config.members;
      mixin.$$destructor = config.destruct;
      mixin.$$type = "Mixin";
      mixin.name = name;
      mixin.toString = this.genericToString;
      rwt.define( name, mixin );
    },


    /**
     * This method will be attached to all mixins to return
     * a nice identifier for them.
     *
     * @internal
     * @return {String} The mixin identifier
     */
    genericToString : function() {
      return "[Mixin " + this.name + "]";
    }


  }

} );
