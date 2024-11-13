/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.version;

import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.Manifest;

public class ManifestGetter {
  public Manifest getManifest() throws Exception {
    URL url = this.getClass().getResource( BuildVersion.REFERENCE_FILE );
    JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
    return jarConnection.getManifest();
  }
}
