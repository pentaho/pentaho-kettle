/*******************************************************************************
 * Copyright (c) 2016 Hitachi America, Ltd., R&D.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hitachi America, Ltd., R&D - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.program;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;

public class Program {
  static final String PREFIX_HTTP = "http://"; //$NON-NLS-1$
  static final String PREFIX_HTTPS = "https://"; //$NON-NLS-1$

  public Program() { }

  public static boolean launch( String fileName ) {
    if ( fileName.startsWith( PREFIX_HTTP ) || fileName.startsWith( PREFIX_HTTPS ) ) {
      UrlLauncher launcher = RWT.getClient().getService( UrlLauncher.class );
      launcher.openURL( fileName );
      return true;
    }
    return false;
  }
}
