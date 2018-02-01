/*!
* Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.pentaho.repositoryvfs.lifecycle;

import org.eclipse.swt.SWT;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.repositoryvfs.dialog.RepositoryVfsProviderDialog;
import org.pentaho.repositoryvfs.vfs.RepositoryVfsProvider;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import com.google.common.base.Supplier;

@LifecyclePlugin( id = "RepositoryVfsKettleLifecycleListener" )
public class RepositoryVfsKettleLifecycleListener implements LifecycleListener {

  private final Supplier<Spoon> spoonSupplier;

  public RepositoryVfsKettleLifecycleListener() {
    this( new Supplier<Spoon>() {
      @Override
      public Spoon get() {
        return Spoon.getInstance();
      }
    } );
  }

  public RepositoryVfsKettleLifecycleListener( Supplier<Spoon> spoonSupplier ) {
    this.spoonSupplier = spoonSupplier;
  }

  @Override
  public void onStart( LifeEventHandler handler ) throws LifecycleException {
    /*
     * Registers the UI for the VFS Browser
     */
    final Spoon spoon = spoonSupplier.get();
    spoon.getDisplay().asyncExec( new Runnable() {
      public void run() {
        VfsFileChooserDialog dialog = spoon.getVfsFileChooserDialog( null, null );
        RepositoryVfsProviderDialog hadoopVfsFileChooserDialog =
            new RepositoryVfsProviderDialog( RepositoryVfsProvider.SCHEME, "Repository VFS", dialog, SWT.NONE );
        dialog.addVFSUIPanel( hadoopVfsFileChooserDialog );
      }
    } );
  }

  @Override
  public void onExit( LifeEventHandler handler ) throws LifecycleException {
  }
}
