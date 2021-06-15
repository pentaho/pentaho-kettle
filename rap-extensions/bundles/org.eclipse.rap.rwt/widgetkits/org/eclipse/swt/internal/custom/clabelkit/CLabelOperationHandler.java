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
package org.eclipse.swt.internal.custom.clabelkit;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.custom.CLabel;


public class CLabelOperationHandler extends ControlOperationHandler<CLabel> {

  private static final String PROP_TEXT = "text";

  public CLabelOperationHandler( CLabel clabel ) {
    super( clabel );
  }

  @Override
  public void handleSet( CLabel clabel, JsonObject properties ) {
    super.handleSet( clabel, properties );
    handleSetText( clabel, properties );
  }

  /*
   * PROTOCOL SET text
   *
   * @param text (String) the new label text
   */
  public void handleSetText( CLabel clabel, JsonObject properties ) {
    JsonValue text = properties.get( PROP_TEXT );
    if( text != null ) {
      clabel.setText( text.asString() );
    }
  }

}
