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
package org.eclipse.swt.internal.widgets.sliderkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Slider;


public class SliderOperationHandler extends ControlOperationHandler<Slider> {

  private static final String PROP_SELECTION = "selection";

  public SliderOperationHandler( Slider slider ) {
    super( slider );
  }

  @Override
  public void handleSet( Slider slider, JsonObject properties ) {
    super.handleSet( slider, properties );
    handleSetSelection( slider, properties );
  }

  @Override
  public void handleNotify( Slider slider, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( slider, properties );
    } else {
      super.handleNotify( slider, eventName, properties );
    }
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection (int) the slider selection value
   */
  public void handleSetSelection( Slider slider, JsonObject properties ) {
    JsonValue value = properties.get( PROP_SELECTION );
    if( value != null ) {
      slider.setSelection( value.asInt() );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   */
  public void handleNotifySelection( Slider slider, JsonObject properties ) {
    Event event = createSelectionEvent( SWT.Selection, properties );
    slider.notifyListeners( SWT.Selection, event );
  }

}
