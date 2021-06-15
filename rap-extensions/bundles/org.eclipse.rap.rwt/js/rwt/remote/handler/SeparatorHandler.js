/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.Separator", {

  factory : function( properties ) {
    var result = new rwt.widgets.Separator();
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    result.setLineOrientation( styleMap.VERTICAL ? "vertical" : "horizontal" );
    var lineStyle = "rwt_SHADOW_NONE";
    if( styleMap.SHADOW_IN ) {
      lineStyle = "rwt_SHADOW_IN";
    } else if( styleMap.SHADOW_OUT ) {
      lineStyle = "rwt_SHADOW_OUT";
    }
    result.setLineStyle( lineStyle );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {} ),

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} )

} );
