/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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
