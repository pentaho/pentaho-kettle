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

package org.pentaho.di.plugins.fileopensave.providers;

import org.pentaho.di.plugins.fileopensave.api.providers.FileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.InvalidFileProviderException;

import java.util.List;

public class ProviderService {

  private final List<FileProvider> fileProviders;

  public ProviderService( List<FileProvider> fileProviders ) {
    this.fileProviders = fileProviders;
  }

  public FileProvider get( String provider ) throws InvalidFileProviderException {
    return fileProviders.stream().filter( fileProvider1 ->
      fileProvider1.getType().equalsIgnoreCase( provider ) && fileProvider1.isAvailable() )
      .findFirst()
      .orElseThrow( InvalidFileProviderException::new );
  }

  public List<FileProvider> get() {
    return fileProviders;
  }

}
