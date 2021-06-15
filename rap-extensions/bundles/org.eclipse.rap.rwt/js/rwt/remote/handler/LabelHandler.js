/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.Label", {

  factory : function( properties ) {
    var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    styleMap.MARKUP_ENABLED = properties.markupEnabled;
    var result = new rwt.widgets.Label( styleMap );
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    /**
     * @name setText
     * @methodOf Label#
     * @description Sets the receivers text to the given string.
     * @param {string} text the new text
     */
    "text",
    "mnemonicIndex",
    "image",
    "alignment",
    "appearance",
    "leftMargin",
    "topMargin",
    "rightMargin",
    "bottomMargin",
    "backgroundGradient"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    "backgroundGradient" : rwt.remote.HandlerUtil.getBackgroundGradientHandler()
  } ),

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} ),

  methods: [ "addListener", "removeListener" ],

  methodHandler : rwt.remote.HandlerUtil.extendListenerMethodHandler( {} ),

  /**
   * @class RWT Scripting analoge to org.eclipse.swt.widgets.Label
   * @name Label
   * @extends Control
   * @description The constructor is not public.
   * @since 2.2
   */
   scriptingMethods : rwt.remote.HandlerUtil.extendControlScriptingMethods(
     /** @lends Label.prototype */
   {
     /**
      * @description  Returns the widget text.
      * @return {string} the widget text
      */
     getText : function() {
       return this.getCellContent( 1 );
     }
   } )
} );
