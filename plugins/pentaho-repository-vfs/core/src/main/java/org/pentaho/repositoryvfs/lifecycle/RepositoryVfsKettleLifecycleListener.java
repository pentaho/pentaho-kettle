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
