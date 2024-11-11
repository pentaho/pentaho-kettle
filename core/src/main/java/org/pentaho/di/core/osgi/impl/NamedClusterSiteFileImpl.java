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

package org.pentaho.di.core.osgi.impl;

import org.pentaho.di.core.osgi.api.NamedClusterSiteFile;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType( name = "SiteFile", description = "Site.xml File contents" )
public class NamedClusterSiteFileImpl implements NamedClusterSiteFile {

  @MetaStoreAttribute
  private String siteFileName;

  @MetaStoreAttribute
  private String siteFileContents;

  @MetaStoreAttribute
  private long sourceFileModificationTime;

  public NamedClusterSiteFileImpl() {

  }

  @Deprecated
  public NamedClusterSiteFileImpl( String siteFileName, String siteFileContents ) {
    this.siteFileName = siteFileName;
    this.siteFileContents = siteFileContents;
  }

  public NamedClusterSiteFileImpl( String siteFileName, long sourceFileModificationTime, String siteFileContents ) {
    this.siteFileName = siteFileName;
    this.siteFileContents = siteFileContents;
    this.sourceFileModificationTime = sourceFileModificationTime;
  }

  @Override
  public String getSiteFileName() {
    return siteFileName;
  }

  @Override
  public void setSiteFileName( String siteFileName ) {
    this.siteFileName = siteFileName;
  }

  @Override
  public String getSiteFileContents() {
    return siteFileContents;
  }

  @Override
  public void setSiteFileContents( String siteFileContents ) {
    this.siteFileContents = siteFileContents;
  }

  @Override
  public NamedClusterSiteFile copy() {
    return new NamedClusterSiteFileImpl( siteFileName, sourceFileModificationTime, siteFileContents );
  }

  @Override
  public long getSourceFileModificationTime() {
    return sourceFileModificationTime;
  }

  @Override
  public void setSourceFileModificationTime( long sourceFileModificationTime ) {
    this.sourceFileModificationTime = sourceFileModificationTime;
  }
}
