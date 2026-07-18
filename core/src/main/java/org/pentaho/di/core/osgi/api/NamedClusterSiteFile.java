/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.core.osgi.api;

public interface NamedClusterSiteFile {
  String getSiteFileName();

  void setSiteFileName( String siteFileName );

  String getSiteFileContents();

  void setSiteFileContents( String siteFileContents );

  NamedClusterSiteFile copy();

  long getSourceFileModificationTime();

  void setSourceFileModificationTime( long sourceFileModificationTime );

}
