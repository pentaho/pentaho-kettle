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

  public void add( FileProvider fileProvider ) {
    // only one provider for a given name and type
    fileProviders.removeIf( p -> p.getType().equalsIgnoreCase( fileProvider.getType() )
      && ( p.getName() == null || p.getName().equalsIgnoreCase( fileProvider.getName() ) ) );
    fileProviders.add( fileProvider );
  }

}
