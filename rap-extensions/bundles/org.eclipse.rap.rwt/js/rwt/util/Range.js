/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource and others.
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
 * This manager is used by all objects which needs ranges like rwt.widgets.base.Spinner, ...
 */
rwt.qx.Class.define( "rwt.util.Range", {

  extend : rwt.qx.Target,

  events: {
    "change" : "rwt.event.Event"
  },

  properties : {

    /** current value of the Range object */
    value : {
      check : "!isNaN(value)&&value>=this.getMin()&&value<=this.getMax()",
      nullable : true,
      event : "change",
      init : 0
    },

    /** maximum fraction digits */
    precision : {
      check : "Integer",
      nullable : true,
      event : "change",
      init : 0
    },

    /** minimal value of the Range object */
    min : {
      check : "Number",
      apply : "_applyMin",
      event : "change",
      init : 0
    },

    /** maximal value of the Range object */
    max : {
      check : "Number",
      apply : "_applyMax",
      event : "change",
      init : 100
    },

    /** whether the value should wrap around */
    wrap : {
      check : "Boolean",
      init : false
    }
  },

  members : {

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyMax : function( value ) {
      this.setValue( Math.min( this.getValue(), value ) );
    },

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyMin : function( value ) {
      this.setValue( Math.max( this.getValue(), value) );
    },

    limit : function( value ) {
      var precision = this.getPrecision();
      if( precision != null ) {
        var mover = Math.pow( 10, precision );
      }

      if (this.getWrap()) {
        if (precision != null) {
          // round to the precision'th digit
          value = Math.round( value * mover ) / mover;
        }

        if (value < this.getMin()) {
          return ( this.getMax() - ( this.getMin() - value ) ) + 1;
        }
        if (value > this.getMax()) {
          return ( this.getMin() + ( value - this.getMax() ) ) - 1;
        }
      }

      if( value < this.getMin() ) {
        return this.getMin();
      }

      if( value > this.getMax() ) {
        return this.getMax();
      }

      if( precision != null ) {
        return Math.round( value * mover ) / mover;
      } else {
        return value;
      }
    }
  }
} );
