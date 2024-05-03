/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.ui;

import org.pentaho.di.connections.annotations.Encrypted;
import org.pentaho.di.connections.vfs.BaseVFSConnectionDetails;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType(
  name = "Test VFS Connection",
  description = "Defines the connection details for a test vfs connection" )
public class TestConnectionDetails extends BaseVFSConnectionDetails {

  private static String TYPE = "test";
  private VariableSpace space;

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

  @Override public VariableSpace getSpace() {
    return space;
  }

  @Override public void setSpace( VariableSpace space ) {
    this.space = space;
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
