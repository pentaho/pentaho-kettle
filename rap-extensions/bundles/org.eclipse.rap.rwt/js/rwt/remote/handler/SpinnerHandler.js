/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.Spinner", {

  factory : function( properties ) {
    var result = new rwt.widgets.Spinner();
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    if( styleMap.READ_ONLY ) {
      result.setEditable( false );
    }
    if( styleMap.WRAP ) {
      result.setWrap( true );
    }
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    // [if] Important: Order matters - minimum, maximum, selection
    "minimum",
    "maximum",
    /**
     * @name setSelection
     * @methodOf Spinner#
     * @description Sets the 'selection', which is the receiver's value, to the argument which
     * must be greater than or equal to zero.
     * @param {int} selection the new selection (must be zero or greater)
     */
    "selection",
    "digits",
    "increment",
    "pageIncrement",
    "textLimit",
    "decimalSeparator"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    "minimum" : function( widget, value ) {
      // [if] Ensures that we don't set min bigger than current max, otherwize and error will be
      // thrown. The correct max is always set by the server in this case.
      var max = widget.getMax();
      if( value > max ) {
        widget.setMax( value + 1 );
      }
      widget.setMin( value );
    },
    "maximum" : function( widget, value ) {
      widget.setMax( value );
    },
    "selection" : function( widget, value ) {
      widget.setValue( value );
    },
    "increment" : function( widget, value ) {
      widget.setIncrementAmount( value );
      widget.setWheelIncrementAmount( value );
    },
    "pageIncrement" : function( widget, value ) {
      widget.setPageIncrementAmount( value );
    },
    "textLimit" : function( widget, value ) {
      widget.setMaxLength( value );
    }
  } ),

  events : [ "Selection", "DefaultSelection", "Modify" ],

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} ),

  methods: [ "addListener", "removeListener" ],

  methodHandler : rwt.remote.HandlerUtil.extendListenerMethodHandler( {} ),

  /**
   * @class RWT Scripting analoge to org.eclipse.swt.widgets.Spinner
   * @name Spinner
   * @extends Control
   * @description The constructor is not public.
   * @since 2.2
   */
  scriptingMethods : rwt.remote.HandlerUtil.extendControlScriptingMethods(
    /** @lends Spinner.prototype */
  {
     /**
      * @description Returns the 'selection', which is the receiver's position.
      * @return {int} the selection
      */
    getSelection : function() {
      return this.getValue();
    },
    /**
     * @description  Returns a string containing a copy of the contents of the
     * receiver's text field, or an empty string if there are no
     * contents.
     * @return {string} the receiver's text
     */
    getText : function() {
      return this._textfield.getValue();
    },
     /**
      * @description Returns the maximum value which the receiver will allow.
      * @return {int} the maximum
      */
    getMaximum : function() {
      return this.getMax();
    },
     /**
      * @description Returns the minimum value which the receiver will allow.
      * @return {int} the minimum
      */
     getMinimum : function() {
      return this.getMin();
    }
  } )

} );
