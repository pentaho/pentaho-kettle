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

rwt.remote.HandlerRegistry.add( "rwt.widgets.Combo", {

  factory : function( properties ) {
    var result = new rwt.widgets.Combo( properties.ccombo );
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    "itemHeight",
    "visibleItemCount",
    "items",
    "listVisible",
    "selectionIndex",
    "editable",
    /**
     * @name setText
     * @methodOf Combo#
     * @description Sets the contents of the receiver's text field to the given string.
     * <p>
     * Note: The text field in a <code>Combo</code> is typically only capable of
     * displaying a single line of text. Thus, setting the text to a string
     * containing line breaks or other special characters will probably cause it
     * to display incorrectly.
     * </p>
     * @param {string} text the new text
     */
    "text",
    /**
     * @name setSelection
     * @methodOf Combo#
     * @description Sets the selection of the text to the range specified
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
    "selectionIndex" : function( widget, value ) {
      widget.select( value );
    },
    "selection" : function( widget, value ) {
      widget.setTextSelection( value );
    }
  } ),

  events : [ "Selection", "DefaultSelection", "Modify" ],

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} ),

  methods: [ "addListener", "removeListener" ],

  methodHandler : rwt.remote.HandlerUtil.extendListenerMethodHandler( {} ),

  /**
   * @class RWT Scripting analoge to org.eclipse.swt.widgets.Combo
   * @name Combo
   * @extends Control
   * @description The constructor is not public.
   * @since 2.2
   */
  scriptingMethods : rwt.remote.HandlerUtil.extendControlScriptingMethods(
    /** @lends Combo.prototype */
  {

    /**
     * @description  Returns the widget text.
     * @return {string} the widget text
     */
    getText : function() {
      return this._field.getValue();
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
      return this._field.getSelection();
    }

  } )

} );
