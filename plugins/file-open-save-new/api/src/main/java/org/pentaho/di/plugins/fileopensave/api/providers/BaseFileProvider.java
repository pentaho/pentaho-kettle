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


package org.pentaho.di.plugins.fileopensave.api.providers;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.ui.core.FileDialogOperation;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public abstract class BaseFileProvider<T extends File> implements FileProvider<T> {
  @Override
  public String sanitizeName( Bowl bowl, T destDir, String newPath ) {
    return newPath;
  }

  @Override
  public void setFileProperties( FileDetails fileDetails, FileDialogOperation fileDialogOperation ) {
    fileDialogOperation.setPath( fileDetails.getPath() );
    fileDialogOperation.setFilename( fileDetails.getName() );
    fileDialogOperation.setConnection( null );
    fileDialogOperation.setProvider( fileDetails.getProvider() );
  }

  @Override
  public List<T> searchFiles( Bowl bowl, T file, String filters, String searchString, VariableSpace space )
    throws FileException {
    List<T> files = getFiles( bowl, file, filters, space );

    return files.stream()
      .filter( resultFile -> StringUtils.isEmpty( searchString )
        || Utils.matches( file.getName(), searchString ) )
      .collect( Collectors.toList() );
  }
}
