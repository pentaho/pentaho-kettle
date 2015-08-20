/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
*
*/

package com.pentaho.repository.importexport;

import java.util.List;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.plugin.services.importer.IPlatformImportHandler;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportFileHandler;


public class PDIImportFileHandler extends RepositoryFileImportFileHandler implements IPlatformImportHandler {

  public PDIImportFileHandler( List<IMimeType> mimeTypes ) {
    super( mimeTypes );
  }

  @Override
  protected RepositoryFile createFile( final RepositoryFileImportBundle bundle, final String repositoryPath,
      final IRepositoryFileData data ) throws PlatformImportException {

    RepositoryFileImportBundle pdiBundle = bundle;
    String originalName = bundle.getName();
    pdiBundle.setName( PDIImportUtil.checkAndSanitize( originalName ) );
    pdiBundle.setTitle( originalName );

    return super.createFile( pdiBundle, repositoryPath, data );
  }

  @Override
  protected RepositoryFile updateFile( final RepositoryFileImportBundle bundle, final RepositoryFile file,
      final IRepositoryFileData data ) throws PlatformImportException {
    RepositoryFile updatedFile = null;
    if ( isNodeRepositoryFileData( file ) ) {
      updatedFile = getRepository().updateFile( file, data, bundle.getComment() );
    } else {
      String fileName = bundle.getName();
      getLogger().trace( "The file [" + fileName + "] will be recreated because it content-type was changed." );
      RepositoryFileAcl originFileAcl = getRepository().getAcl( file.getId() );
      getRepository().deleteFile( file.getId(), true, null );

      RepositoryFileAcl newFileAcl = bundle.getAcl();
      bundle.setAcl( originFileAcl );
      updatedFile = createFile( bundle, file.getPath(), data );
      bundle.setAcl( newFileAcl );
    }
    return updatedFile;
  }

  private boolean isNodeRepositoryFileData( final RepositoryFile file ) {
    try {
      getRepository().getDataForRead( file.getId(), NodeRepositoryFileData.class );
      return true;
    } catch ( UnifiedRepositoryException e ) {
      return false;
    }
  }
}
