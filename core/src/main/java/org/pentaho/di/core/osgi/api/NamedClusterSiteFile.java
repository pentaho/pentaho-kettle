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
