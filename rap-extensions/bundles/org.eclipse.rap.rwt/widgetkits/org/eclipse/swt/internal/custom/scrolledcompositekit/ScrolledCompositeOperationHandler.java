/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.custom.scrolledcompositekit;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;


public class ScrolledCompositeOperationHandler extends ControlOperationHandler<ScrolledComposite> {

  private static final String PARAM_H_BAR_SELECTION = "horizontalBar.selection";
  private static final String PARAM_V_BAR_SELECTION = "verticalBar.selection";

  public ScrolledCompositeOperationHandler( ScrolledComposite composite ) {
    super( composite );
  }

  @Override
  public void handleSet( ScrolledComposite composite, JsonObject properties ) {
    super.handleSet( composite, properties );
    handleSetOrigin( composite, properties );
  }

  /*
   * PROTOCOL SET origin
   *
   * @param horizontalBar.selection (int) value of the horizontal scrollbar
   * @param verticalBar.selection (int) value of the vertical scrollbar
   */
  public void handleSetOrigin( ScrolledComposite composite, JsonObject properties ) {
    Point origin = composite.getOrigin();
    JsonValue hSelection = properties.get( PARAM_H_BAR_SELECTION );
    if( hSelection != null && composite.getHorizontalBar() != null ) {
      origin.x = hSelection.asInt();
    }
    JsonValue vSelection = properties.get( PARAM_V_BAR_SELECTION );
    if( vSelection != null && composite.getVerticalBar() != null ) {
      origin.y = vSelection.asInt();
    }
    composite.setOrigin( origin );
  }

}
