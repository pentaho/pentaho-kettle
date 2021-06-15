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
package org.eclipse.swt.internal.widgets.progressbarkit;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.widgets.ProgressBar;


public class ProgressBarOperationHandler extends ControlOperationHandler<ProgressBar> {

  private static final String PROP_SELECTION = "selection";

  public ProgressBarOperationHandler( ProgressBar progressBar ) {
    super( progressBar );
  }

  @Override
  public void handleSet( ProgressBar progressBar, JsonObject properties ) {
    super.handleSet( progressBar, properties );
    handleSetSelection( progressBar, properties );
  }

  /*
   * PROTOCOL SET selection
   *
   * @param selection (int) the new progress bar selection
   */
  public void handleSetSelection( ProgressBar progressBar, JsonObject properties ) {
    JsonValue selection = properties.get( PROP_SELECTION );
    if( selection != null ) {
      progressBar.setSelection( selection.asInt() );
    }
  }

}
