/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.progressbarkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderClientListeners;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ProgressBar;


public class ProgressBarLCA extends WidgetLCA<ProgressBar> {

  public static final ProgressBarLCA INSTANCE = new ProgressBarLCA();

  private static final String TYPE = "rwt.widgets.ProgressBar";
  private static final String[] ALLOWED_STYLES = {
    "SMOOTH", "HORIZONTAL", "VERTICAL", "INDETERMINATE", "BORDER"
  };

  static final String PROP_MINIMUM = "minimum";
  static final String PROP_MAXIMUM = "maximum";
  static final String PROP_SELECTION = "selection";
  static final String PROP_STATE = "state";

  // Default values
  private static final int DEFAULT_MINIMUM = 0;
  private static final int DEFAULT_MAXIMUM = 100;
  private static final int DEFAULT_SELECTION = 0;
  private static final String DEFAULT_STATE = "normal";

  @Override
  public void preserveValues( ProgressBar progressBar ) {
    preserveProperty( progressBar, PROP_MINIMUM, Integer.valueOf( progressBar.getMinimum() ) );
    preserveProperty( progressBar, PROP_MAXIMUM, Integer.valueOf( progressBar.getMaximum() ) );
    preserveProperty( progressBar, PROP_SELECTION, Integer.valueOf( progressBar.getSelection() ) );
    preserveProperty( progressBar, PROP_STATE, getState( progressBar ) );
  }

  @Override
  public void renderInitialization( ProgressBar progressBar ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( progressBar, TYPE );
    remoteObject.setHandler( new ProgressBarOperationHandler( progressBar ) );
    remoteObject.set( "parent", getId( progressBar.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( progressBar, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( ProgressBar progressBar ) throws IOException {
    ControlLCAUtil.renderChanges( progressBar );
    WidgetLCAUtil.renderCustomVariant( progressBar );
    renderProperty( progressBar, PROP_MINIMUM, progressBar.getMinimum(), DEFAULT_MINIMUM );
    renderProperty( progressBar, PROP_MAXIMUM, progressBar.getMaximum(), DEFAULT_MAXIMUM );
    renderProperty( progressBar, PROP_SELECTION, progressBar.getSelection(), DEFAULT_SELECTION );
    renderProperty( progressBar, PROP_STATE, getState( progressBar ), DEFAULT_STATE );
    renderClientListeners( progressBar );
  }

  private static String getState( ProgressBar progressBar ) {
    String result = "normal";
    int state = progressBar.getState();
    if( state == SWT.ERROR ) {
      result = "error";
    } else if( state == SWT.PAUSED ) {
      result = "paused";
    }
    return result;
  }

  private ProgressBarLCA() {
    // prevent instantiation
  }

}
