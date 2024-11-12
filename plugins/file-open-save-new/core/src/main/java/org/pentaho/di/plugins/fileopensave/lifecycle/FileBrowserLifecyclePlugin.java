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


package org.pentaho.di.plugins.fileopensave.lifecycle;

import org.pentaho.di.plugins.fileopensave.cache.FileCache;
import org.pentaho.di.plugins.fileopensave.service.FileCacheService;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

import java.util.function.Supplier;

/**
 * Listens for spoon lifecycle events
 */
@SpoonPlugin( id = "FileBrowserLifecyclePlugin", image = "" )
public class FileBrowserLifecyclePlugin implements SpoonPluginInterface, SpoonLifecycleListener {

  public static final String VFS_CONNECTIONS = "VFS Connections";
  private final FileCache fileCache;
  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  public FileBrowserLifecyclePlugin() {
    this( FileCacheService.INSTANCE.get() );
  }
  
  public FileBrowserLifecyclePlugin( FileCache fileCache ) {
    this.fileCache = fileCache;
  }

  /**
   * Capture event and clear the file cache when repository is changed
   *
   * @param evt
   */
  @Override public void onEvent( SpoonLifeCycleEvent evt ) {
    if ( evt == SpoonLifeCycleEvent.REPOSITORY_CHANGED ) {
      fileCache.clearAll();
      spoonSupplier.get().refreshTree( VFS_CONNECTIONS );
    }
    if ( evt == SpoonLifeCycleEvent.REPOSITORY_DISCONNECTED ) {
      spoonSupplier.get().refreshTree( VFS_CONNECTIONS );
    }
  }

  @Override public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    // No UI elements to attach
  }

  @Override public SpoonLifecycleListener getLifecycleListener() {
    return this;
  }

  @Override public SpoonPerspective getPerspective() {
    return null;
  }
}
