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

package org.pentaho.di.plugins.fileopensave.lifecycle;

import org.pentaho.di.plugins.fileopensave.cache.FileCache;
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
