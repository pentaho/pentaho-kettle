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

import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;

/**
 * An extension point for handling import of named connections from the embedded meta store
 */
@ExtensionPoint(
  id = "JobAfterOpenExtensionPoint",
  extensionPointId = "JobAfterOpen",
  description = "Save named connections in the connected meta store"
)
public final class JobAfterOpenExtensionPoint extends MetaAfterOpenExtensionPoint {
  public JobAfterOpenExtensionPoint( MetastoreLocator metastoreLocator ) {
    super( metastoreLocator );
  }
}
