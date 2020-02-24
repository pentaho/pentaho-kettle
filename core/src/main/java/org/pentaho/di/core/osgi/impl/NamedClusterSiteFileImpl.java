/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

  public NamedClusterSiteFileImpl() {

  }

  public NamedClusterSiteFileImpl( String siteFileName, String siteFileContents ) {
    this.siteFileName = siteFileName;
    this.siteFileContents = siteFileContents;
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
    return new NamedClusterSiteFileImpl( siteFileName, siteFileContents );
  }
}
