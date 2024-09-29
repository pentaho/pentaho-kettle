/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.metastore.locator.api.impl;

import org.pentaho.di.core.extension.ExtensionPoint;

/**
 * Created by tkafalas on 8/9/2017.
 * <p>
 * This class exists because two ExtensionPoint annotations are not allowed on the same class.
 */
@ExtensionPoint( id = "MetastoreLocatorBeforeJobSaveExtensionPoint", extensionPointId = "JobBeforeSave",
  description = "" )
public class MetastoreLocatorBeforeJobSaveExtensionPoint extends MetastoreLocatorExtensionPoint {
  }
