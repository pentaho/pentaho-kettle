/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public abstract class AbstractBundleRef {

  private static final Log log = LogFactory.getLog( AbstractBundleRef.class );

  public String getVersion() {
    String licenseVersion = null;
    try {
      Enumeration<URL> resources = getClass().getClassLoader()
              .getResources( "META-INF/MANIFEST.MF" );
      Attributes.Name keyAttribute = new Attributes.Name( getKey() );
      while ( resources.hasMoreElements() && licenseVersion == null ) {
        Manifest manifest = new Manifest( resources.nextElement().openStream() );
        licenseVersion = (String) manifest.getMainAttributes().get( keyAttribute );
      }
    } catch ( Exception e ) {
      log.error( e );
    }

    if ( licenseVersion == null ) {
      log.warn( getKey() + " not found in manifest file" );
    }

    return licenseVersion;
  }

  protected abstract String getKey();
}
