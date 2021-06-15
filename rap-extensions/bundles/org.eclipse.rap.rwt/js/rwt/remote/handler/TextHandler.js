/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.Text", {

  factory : function( properties ) {
    var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    var result = new rwt.widgets.Text( styleMap.MULTI );
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    if( styleMap.RIGHT ) {
      result.setTextAlign( "right" );
    } else if( styleMap.CENTER ) {
      result.setTextAlign( "center" );
    }
    result.setWrap( styleMap.WRAP !== undefined );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    /**
     * @name setText
     * @methodOf Text#
     * @description Sets the contents of the receiver to the given string. If the receiver has style
     * SINGLE and the argument contains multiple lines of text, the result of this
     * operation is undefined and may vary from platform to platform.
     * @param {string} text the new text
     */
    "text",
    "message",
    "echoChar",
    "editable",
    /**
     * @name setSelection
     * @methodOf Text#
     * @description Sets the selection to the range specified
     * by an array whose first value is the
     * character position representing the start of the selected
     * text, and whose second value is the character position
     * representing the end of the selection. An "empty" selection
     * is indicated by the values being identical.
     * <p>
     * Indexing is zero based.  The range of
     * a selection is from 0..N where N is
     * the number of characters in the widget.
     * </p><p>
     * Text selections are specified in terms of
     * caret positions.  In a text widget that
     * contains N characters, there are N+1 caret
     * positions, ranging from 0..N.  This differs
     * from other functions that address character
     * position such as getText () that use the
     * usual array indexing rules.
     * </p>
     *
     * @param {int[]} selection array representing the selection start and end
     */
    "selection",
    "textLimit"
  ] ),



  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    "text" : function( widget, value ) {
      var EncodingUtil = rwt.util.Encoding;
      var text = EncodingUtil.truncateAtZero( value );
      if( !widget.hasState( "rwt_MULTI" ) ) {
        text = EncodingUtil.replaceNewLines( text, " " );
      }
      widget.setValue( text );
    },
    "echoChar" : function( widget, value ) {
      if( !widget.hasState( "rwt_MULTI" ) ) {
        widget.setPasswordMode( value !== null );
      }
    },
    "editable" : function( widget, value ) {
      widget.setReadOnly( !value );
    },
    "textLimit" : function( widget, value ) {
      widget.setMaxLength( value );
    }
  } ),

  events : [ "DefaultSelection", "Modify" ],

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} ),

  methods: [ "addListener", "removeListener" ],

  methodHandler : rwt.remote.HandlerUtil.extendListenerMethodHandler( {} ),

  /**
   * @class RWT Scripting analoge to org.eclipse.swt.widgets.Text
   * @name Text
   * @extends Control
   * @description The constructor is not public.
   * @since 2.2
   */
  scriptingMethods : rwt.remote.HandlerUtil.extendControlScriptingMethods(
    /** @lends Text.prototype */
  {

    /**
     * @description  Returns the widget text.
     * @return {string} the widget text
     */
    getText : function() {
      return this.getValue();
    },

    /**
     * @description Returns an array whose first value is the
     * character position representing the start of the selected
     * text, and whose second value is the character position
     * representing the end of the selection. An "empty" selection
     * is indicated by the values being identical.
     * <p>
     * Indexing is zero based.  The range of a selection is from
     * 0..N where N is the number of characters in the widget.
     * </p>
     *
     * @return {int[]} array representing the selection start and end
     */
    getSelection : function() {
      return this.getSelection();
    },

    /**
     * @description  Returns the editable state.
     * @return {boolean} whether or not the receiver is editable
     */
    getEditable : function() {
      return !this.getReadOnly();
    }

  } )

} );
