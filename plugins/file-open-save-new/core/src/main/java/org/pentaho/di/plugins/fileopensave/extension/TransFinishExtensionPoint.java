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


package org.pentaho.di.plugins.fileopensave.extension;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.plugins.fileopensave.cache.FileCache;
import org.pentaho.di.plugins.fileopensave.service.FileCacheService;

@ExtensionPoint(
  id = "TransFinishExtensionPoint",
  extensionPointId = "TransformationFinish",
  description = "Clear file browser cache when Job finishes"
)
public class TransFinishExtensionPoint implements ExtensionPointInterface {

  private final FileCache fileCache;

  public TransFinishExtensionPoint() {
    this( FileCacheService.INSTANCE.get() );
  }
  
  public TransFinishExtensionPoint( FileCache fileCache ) {
    this.fileCache = fileCache;
  }

  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    fileCache.clearAll();
  }
}
