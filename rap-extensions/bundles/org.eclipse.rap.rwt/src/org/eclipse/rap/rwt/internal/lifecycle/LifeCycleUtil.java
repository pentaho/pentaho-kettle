/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.widgets.Display;


public class LifeCycleUtil {
  private static final String ATTR_SESSION_DISPLAY
    = LifeCycleUtil.class.getName() + "#sessionDisplay";
  private static final String ATTR_UI_THREAD = LifeCycleUtil.class.getName() + "#uiThread";

  public static void setSessionDisplay( Display display ) {
    ContextProvider.getUISession().setAttribute( ATTR_SESSION_DISPLAY, display );
  }

  public static Display getSessionDisplay() {
    Display result = null;
    if( ContextProvider.hasContext() ) {
      UISession uiSession = ContextProvider.getUISession();
      result = getSessionDisplay( uiSession );
    }
    return result;
  }

  public static Display getSessionDisplay( UISession uiSession ) {
    return ( Display )uiSession.getAttribute( ATTR_SESSION_DISPLAY );
  }

  public static void setUIThread( UISession uiSession, IUIThreadHolder threadHolder ) {
    uiSession.setAttribute( ATTR_UI_THREAD, threadHolder );
  }

  public static IUIThreadHolder getUIThread( UISession uiSession ) {
    return ( IUIThreadHolder )uiSession.getAttribute( ATTR_UI_THREAD );
  }

  public static boolean isStartup() {
    return getSessionDisplay() == null;
  }

  private LifeCycleUtil() {
    // prevent instantiation
  }
}
