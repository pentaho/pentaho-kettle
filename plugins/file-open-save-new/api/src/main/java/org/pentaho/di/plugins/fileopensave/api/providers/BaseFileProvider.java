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

package org.pentaho.di.plugins.fileopensave.api.providers;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.ui.core.FileDialogOperation;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseFileProvider<T extends File> implements FileProvider<T> {
  @Override public String sanitizeName( T destDir, String newPath ) {
    return newPath;
  }

  @Override public void setFileProperties( FileDetails fileDetails, FileDialogOperation fileDialogOperation ) {
    fileDialogOperation.setPath( fileDetails.getPath() );
    fileDialogOperation.setFilename( fileDetails.getName() );
    fileDialogOperation.setConnection( null );
    fileDialogOperation.setProvider( fileDetails.getProvider() );
  }

  @Override public List<T> searchFiles( T file, String filters, String searchString, VariableSpace space )
    throws FileException {
    List<T> files = getFiles( file, filters, space );

    return files.stream()
      .filter( resultFile -> StringUtils.isEmpty( searchString )
        || Utils.matches( file.getName(), searchString ) )
      .collect( Collectors.toList() );
  }
}
