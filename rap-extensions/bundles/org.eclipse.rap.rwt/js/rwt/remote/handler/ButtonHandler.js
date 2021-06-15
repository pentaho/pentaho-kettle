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

rwt.remote.HandlerRegistry.add( "rwt.widgets.Button", {

  factory : function( properties ) {
    var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    var buttonType = "push";
    if( styleMap.CHECK ) {
      buttonType = "check";
    } else if( styleMap.TOGGLE ) {
      buttonType = "toggle";
    } else if( styleMap.RADIO ) {
      buttonType = "radio";
    } else if( styleMap.ARROW ) {
      buttonType = "arrow";
    }
    var result = new rwt.widgets.Button( buttonType );
    result.setMarkupEnabled( properties.markupEnabled === true );
    result.setWrap( styleMap.WRAP );
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
      result.setNoRadioGroup( parent.hasState( "rwt_NO_RADIO_GROUP" ) );
    } );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    /**
     * @name setText
     * @methodOf Button#
     * @description Sets the receivers text to the given string.
     * @param {string} text the new text
     */
    "text",
    "mnemonicIndex",
    "alignment",
    "image",
    /**
     * @name setSelection
     * @methodOf Button#
     * @description Sets the selection state of the receiver, if it is of type <code>CHECK</code>,
     * <code>RADIO</code>, or <code>TOGGLE</code>.
     *
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked. When it is of type <code>TOGGLE</code>,
     * it is selected when it is pushed in.
     * </p>
     * @param {boolean} selected the new selection state
     */
    "selection",
    "grayed",
    "badge"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    "image" : function( widget, value ) {
      if( value === null ) {
        widget.setImage( value );
      } else {
        widget.setImage.apply( widget, value );
      }
    },
    "badge" : function( widget, value ) {
      rwt.widgets.util.Badges.setBadge( widget, value );
    }
  } ),

  events : [ "Selection" ],

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} ),

  methods: [ "addListener", "removeListener" ],

  methodHandler : rwt.remote.HandlerUtil.extendListenerMethodHandler( {} ),

  /**
   * @class RWT Scripting analoge to org.eclipse.swt.widgets.Button
   * @name Button
   * @extends Control
   * @description The constructor is not public.
   * @since 2.2
   */
  scriptingMethods : rwt.remote.HandlerUtil.extendControlScriptingMethods(
    /** @lends Button.prototype */
  {
    /**
     * @description  Returns the widget text.
     * @return {string} the widget text
     */
    getText : function() {
      return this.getCellContent( 2 );
    },
    /**
     * @description Returns <code>true</code> if the receiver is selected,
     * and false otherwise.
     * <p>
     * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
     * it is selected when it is checked. When it is of type <code>TOGGLE</code>,
     * it is selected when it is pushed in. If the receiver is of any other type,
     * this method returns false.
     *</p>
     * @return {boolean} the selection state
     */
    getSelection : function() {
      return this.hasState( "selected" );
    }
  } )

} );
