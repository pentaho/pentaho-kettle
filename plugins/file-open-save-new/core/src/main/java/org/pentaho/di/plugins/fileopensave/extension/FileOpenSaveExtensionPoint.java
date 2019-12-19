/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2019 by Hitachi Vantara : http://www.pentaho.com
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

  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;
  private final ProviderService providerService;

  public FileOpenSaveExtensionPoint( ProviderService providerService ) {
    this.providerService = providerService;
  }

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    FileDialogOperation fileDialogOperation = (FileDialogOperation) o;

    final FileOpenSaveDialog fileOpenSaveDialog =
      new FileOpenSaveDialog( spoonSupplier.get().getShell(), WIDTH, HEIGHT, logChannelInterface );

    resolveProvider( fileDialogOperation );
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

  private void resolveProvider( FileDialogOperation op ) {
    if ( op.getProvider() == null ) {
      if ( op.getConnection() != null ) {
        op.setProvider( VFSFileProvider.TYPE );
      } else if ( spoonSupplier.get().rep != null ) {
        op.setProvider( RepositoryFileProvider.TYPE );
      } else {
        op.setProvider( LocalFileProvider.TYPE );
      }
    }
  }
}
