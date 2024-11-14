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


package org.pentaho.di.connections.common.bucket;

import org.pentaho.metastore.persist.MetaStoreAttribute;

/**
 * Child class to validate encrypted fields in Parent Classes getting encrypted without failure
 */
public class TestConnectionWithBucketsDetailsChild extends TestConnectionWithBucketsDetails {

  @MetaStoreAttribute
  private String password3;

  public void setPassword3( String password3 ) {
    this.password3 = password3;
  }

  public String getPassword3() {
    return password3;
  }
}
