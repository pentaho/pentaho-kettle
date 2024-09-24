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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.plugins.fileopensave.api.providers.FileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.InvalidFileProviderException;
import org.pentaho.di.plugins.fileopensave.dialog.FileOpenSaveDialog;
import org.pentaho.di.plugins.fileopensave.providers.ProviderService;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;
import org.pentaho.di.plugins.fileopensave.service.ProviderServiceService;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.function.Supplier;

/**
 * Created by bmorrise on 5/23/17.
 */

@ExtensionPoint(
        id = "FileOpenSaveNewExtensionPoint",
        extensionPointId = "SpoonOpenSaveNew",
        description = "Open the new file browser"
)
public class FileOpenSaveExtensionPoint implements ExtensionPointInterface {

  private static final int WIDTH = ( Const.isOSX() || Const.isLinux() ) ? 930 : 947;
  private static final int HEIGHT = ( Const.isOSX() || Const.isLinux() ) ? 618 : 626;

  private final Supplier<Spoon> spoonSupplier;
  private final ProviderService providerService;

  public FileOpenSaveExtensionPoint() {
    this( ProviderServiceService.get() );
  }

  public FileOpenSaveExtensionPoint( ProviderService providerService ) {
    this( providerService, Spoon::getInstance );
  }

  public FileOpenSaveExtensionPoint( ProviderService providerService, Supplier<Spoon> spoonSupplier  ) {
    this.providerService = providerService;
    this.spoonSupplier = spoonSupplier;
  }

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    FileDialogOperation fileDialogOperation = (FileDialogOperation) o;

    resolveProvider( fileDialogOperation );

    final FileOpenSaveDialog fileOpenSaveDialog =
            new FileOpenSaveDialog( spoonSupplier.get().getShell(), WIDTH, HEIGHT, logChannelInterface );

    fileOpenSaveDialog.setProviderFilter( fileDialogOperation.getProviderFilter() );
    fileOpenSaveDialog.open( fileDialogOperation );

    fileDialogOperation.setPath( null );
    fileDialogOperation.setFilename( null );
    fileDialogOperation.setConnection( null );


    if ( !Utils.isEmpty( fileOpenSaveDialog.getProvider() ) ) {
      try {
        FileProvider fileProvider = providerService.get( fileOpenSaveDialog.getProvider() );
        fileProvider.setFileProperties( fileOpenSaveDialog, fileDialogOperation );
      } catch ( InvalidFileProviderException e ) {
        throw new KettleException( e );
      }
    }
  }

  /**
   * Calls {@link FileDialogOperation#setProvider(String)} for <code>op</code> with a best guess.
   * @param op
   */
  protected void resolveProvider( FileDialogOperation op ) {
    if ( op.getProvider() == null ) {
      if ( isVfsPath( op.getPath() ) ) {
        op.setProvider( VFSFileProvider.TYPE );
      } else if ( spoonSupplier.get().getRepository() != null ) {
        op.setProvider( RepositoryFileProvider.TYPE );
      } else {
        op.setProvider( LocalFileProvider.TYPE );
      }
    }
  }

  /**
   * Determines if the <code>filePath</code> is a file from a provider of type {@value VFSFileProvider#TYPE}
   * @param filePath
   * @return file from a provider of type {@value VFSFileProvider#TYPE}, false otherwise
   */
  protected boolean isVfsPath( String filePath ) {
    boolean ret = false;
    try {
      VFSFileProvider vfsFileProvider = (VFSFileProvider) providerService.get( VFSFileProvider.TYPE );
      if ( vfsFileProvider == null ) {
        return false;
      }
      return vfsFileProvider.isSupported( filePath );
    } catch ( InvalidFileProviderException | ClassCastException e ) {
      // DO NOTHING
    }
    return ret;
  }
}
