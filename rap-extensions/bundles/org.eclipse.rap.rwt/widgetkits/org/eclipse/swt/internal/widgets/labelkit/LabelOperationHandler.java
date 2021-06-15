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
package org.eclipse.swt.internal.widgets.labelkit;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.widgets.Label;


public class LabelOperationHandler extends ControlOperationHandler<Label> {

  private static final String PROP_TEXT = "text";

  public LabelOperationHandler( Label label ) {
    super( label );
  }

  @Override
  public void handleSet( Label label, JsonObject properties ) {
    super.handleSet( label, properties );
    handleSetText( label, properties );
  }

  /*
   * PROTOCOL SET text
   *
   * @param text (String) the new label text
   */
  public void handleSetText( Label label, JsonObject properties ) {
    JsonValue text = properties.get( PROP_TEXT );
    if( text != null ) {
      label.setText( text.asString() );
    }
  }

}
