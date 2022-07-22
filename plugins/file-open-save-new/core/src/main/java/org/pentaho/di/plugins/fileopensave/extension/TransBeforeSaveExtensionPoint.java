/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.extension;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;

/**
 * An extension point for exporting named connections to the embedded meta store
 */
@ExtensionPoint(
  id = "TransBeforeSaveExtensionPoint",
  extensionPointId = "TransBeforeSave",
  description = "Save named connections in embedded meta store"
)
public final class TransBeforeSaveExtensionPoint extends MetaStoreCopyExtensionPoint {

  public TransBeforeSaveExtensionPoint( MetastoreLocator metastoreLocator ) {
    super( metastoreLocator );
  }

  /**
   * Export name connections from connected meta store to embedded meta store
   *
   * @param log the logging channel to log debugging information to
   * @param object The subject object that is passed to the plugin code
   * @throws KettleException A kettle exception
   */
  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    exportAll( (AbstractMeta) object );
  }
}
