/*******************************************************************************
 * Copyright (c) 2012, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.displaykit;

import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.service.UISession;


public class ServerPushRenderer {

  public static final String REMOTE_OBJECT_ID = "rwt.client.ServerPush";
  private static final String PROP_ACTIVE = "active";
  private static final String ATTR_PRESERVED_ACTIVATION
    = ServerPushRenderer.class.getName() + ".preservedActivation";

  private final UISession uiSession;
  private final ServerPushManager pushManager;

  ServerPushRenderer() {
    uiSession = ContextProvider.getUISession();
    pushManager = ServerPushManager.getInstance();
  }

  void render() {
    boolean activation = pushManager.needsActivation();
    if( mustRender( activation ) ) {
      // Note [rst] server push activation can be changed at any time by a background thread.
      //            Therefore we need to preserve the same value that is rendered to the client.
      renderActivation( activation );
      preserveActivation( activation );
    }
  }

  private boolean mustRender( boolean activation ) {
    boolean result = hasChanged( activation );
    // do not render deactivation if there are pending runnables
    if( result && !activation && pushManager.hasRunnables() ) {
      result = false;
    }
    return result;
  }

  private boolean hasChanged( boolean activation ) {
    return activation != getPreservedActivation();
  }

  private void preserveActivation( boolean activation ) {
    uiSession.setAttribute( ATTR_PRESERVED_ACTIVATION, Boolean.valueOf( activation ) );
  }

  private boolean getPreservedActivation() {
    Boolean preserved = ( Boolean )uiSession.getAttribute( ATTR_PRESERVED_ACTIVATION );
    return preserved != null ? preserved.booleanValue() : false;
  }

  void renderActivation( boolean activation ) {
    ProtocolMessageWriter writer = ContextProvider.getProtocolWriter();
    writer.appendSet( REMOTE_OBJECT_ID, PROP_ACTIVE, activation );
  }

}
