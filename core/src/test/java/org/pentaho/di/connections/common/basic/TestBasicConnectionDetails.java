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


package org.pentaho.di.connections.common.basic;

import org.pentaho.di.connections.annotations.Encrypted;
import org.pentaho.di.connections.vfs.BaseVFSConnectionDetails;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType(
  name = "Test VFS Connection With No Domain and No Buckets",
  description = "Defines the connection details for a test vfs connection" )
public class TestBasicConnectionDetails extends TestBaseVFSConnectionDetails {

  private static String TYPE = "test4";

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String description;

  @Encrypted
  @MetaStoreAttribute
  private String password;

  @Encrypted
  @MetaStoreAttribute
  private String password1;

  @Override public boolean hasBuckets() {
    return false;
  }

  @Override public String getName() {
    return name;
  }

  @Override public void setName( String name ) {
    this.name = name;
  }

  @Override public String getType() {
    return TYPE;
  }

  @Override public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public String getPassword1() {
    return password1;
  }

  public void setPassword1( String password1 ) {
    this.password1 = password1;
  }

}
