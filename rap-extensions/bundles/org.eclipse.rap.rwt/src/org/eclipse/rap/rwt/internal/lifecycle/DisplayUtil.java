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
package org.eclipse.rap.rwt.internal.lifecycle;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.internal.widgets.displaykit.DisplayLCA;
import org.eclipse.swt.widgets.Display;


public final class DisplayUtil {

  private DisplayUtil() {
    // prevent instance creation
  }

  public static DisplayLCA getLCA( Display display ) {
    UISession uiSession = display.getAdapter( IDisplayAdapter.class ).getUISession();
    return SingletonUtil.getUniqueInstance( DisplayLCA.class, uiSession.getApplicationContext() );
  }

  public static String getId( Display display ) {
    return getAdapter( display ).getId();
  }

  public static RemoteAdapter getAdapter( Display display ) {
    RemoteAdapter result = display.getAdapter( RemoteAdapter.class );
    if( result == null ) {
      throw new IllegalStateException( "Could not retrieve an instance of RemoteAdapter." );
    }
    return result;
  }

}
